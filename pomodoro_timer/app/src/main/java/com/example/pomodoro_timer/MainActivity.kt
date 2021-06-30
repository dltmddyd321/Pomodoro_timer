package com.example.pomodoro_timer

import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private val remainMinutesTextView: TextView by lazy{
        findViewById(R.id.remainMinTextView)
    }

    private val remainSecondsTextView: TextView by lazy {
        findViewById(R.id.remainSecondsTextView)
    }

    private val seekBar: SeekBar by lazy {
        findViewById(R.id.seek)
    }

    private var tickingSoundId: Int? = null

    private var bellSoundId: Int? = null

    private val soundPool = SoundPool.Builder().build()
    //사운드 파일 사용

    private var currentCountDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        initSounds()
    }

    override fun onResume() {
        super.onResume()
        soundPool.autoResume()
    }

    override fun onPause() {
        super.onPause()
        soundPool.autoPause()
        //앱을 닫으면 사운드 정지
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }

    private fun bindViews() {
        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if(fromUser) {
                        updateRemainTime(progress * 60 * 1000L)
                    } //00에서 바로 58로 넘어가는 문제 수정
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    stopCountDown()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar ?: return

                    if(seekBar.progress ==0) {
                        stopCountDown()
                    } else {
                        startCountDown()
                    }
                }
            }
        )
    }

    private fun stopCountDown(){
        currentCountDownTimer?.cancel()
        currentCountDownTimer = null
        //SeekBar를 다시 스크롤하면 새로운 기준부터 타이머가 시작

        soundPool.autoPause()
        //카운트 다운이 끝나면 타이머 소리도 정지
    }

    //얼마의 시간 뒤 종료하고, 얼마의 간격으로 실행할지 선언
    private fun createCountDownTimer(initialMillis: Long): CountDownTimer {
        return object : CountDownTimer(initialMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) { //실행 중
                updateRemainTime(millisUntilFinished)
                updateSeekBar(millisUntilFinished)
            }

            override fun onFinish() { //종료
                completeCountDown()
            }
        }
    }

    private fun startCountDown() {
        currentCountDownTimer = createCountDownTimer(seekBar.progress * 60 * 1000L)
        currentCountDownTimer?.start()
        //SeekBar 수치에 따라 타이머 시작

        tickingSoundId?.let { soundId ->
            soundPool.play(soundId,1F,1F,0,-1,1F)
        } //null값이 아닐 경우 soundId를 let으로 받아 인자 값 활용, 사운드 재생 시작
    }

    private fun completeCountDown() {
        updateRemainTime(0)
        updateSeekBar(0)

        soundPool.autoPause()
        bellSoundId?.let { soundId ->
            soundPool.play(soundId,1F,1F,0,0,1F)
        } //카운트 다운이 끝나면 알람 사운드 효과 발생
    }

    private fun initSounds() {
        tickingSoundId = soundPool.load(this,R.raw.timer_ticking, 1)
        bellSoundId = soundPool.load(this,R.raw.timer_bell,1)
    }

    private fun updateRemainTime(remainMillis: Long) {
        val remainSeconds = remainMillis/1000

        remainMinutesTextView.text = "%02d'".format(remainSeconds / 60) //몫이 분
        remainSecondsTextView.text = "%02d".format(remainSeconds % 60) //나머지가 초
    }

    private fun updateSeekBar(remainMillis: Long) {
        seekBar.progress = (remainMillis / 1000 / 60).toInt() //Long -> Int
    }
}