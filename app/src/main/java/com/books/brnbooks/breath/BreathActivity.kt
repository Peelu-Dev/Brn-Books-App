package com.books.brnbooks.breath


import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.media.SoundPool
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.ArrayAdapter
import android.media.AudioManager
import android.os.*
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.books.brnbooks.R
import com.books.brnbooks.books.main.Constants
import com.books.brnbooks.books.screens.DonateActivity
import com.books.brnbooks.breath.data.Config
import com.books.brnbooks.databinding.ActivityBreathBinding
import com.books.brnbooks.breath.main.BreatheContract
import com.books.brnbooks.breath.main.BreathePresenter


class BreathActivity : AppCompatActivity(), BreatheContract.View {

    private lateinit var presenter: BreatheContract.Presenter
    private lateinit var binding: ActivityBreathBinding
    private lateinit var animatorSet: AnimatorSet
    private lateinit var startAnimator : ValueAnimator
    private lateinit var inhaleAnimator : ValueAnimator
    private lateinit var exhaleAnimator : ValueAnimator
    private lateinit var finishAnimator : ValueAnimator
    private var breathing: Boolean = false
    private var config: Config = Config()
    private lateinit var vibrator : Vibrator
    private lateinit var soundPool : SoundPool
    private var soundId : Int = 0
    private lateinit var timer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBreathBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Prevent phone from going to sleep.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // On circle click start breathing.
        binding.viewCircle.setOnClickListener {
            // Verify if breathing has not started.
            if(!breathing) {
                performNotification()
                // Set time of breathing.
                when (binding.spinnerMinutes.selectedItemPosition) {
                    0 -> config.minutes = 1
                    1 -> config.minutes = 2
                    2 -> config.minutes = 3
                    3 -> config.minutes = 5
                    4 -> config.minutes = 10
                }
                presenter.startBreathing(config)
            }
        }


        binding.donate.setOnClickListener {
            startActivity(Intent(this,DonateActivity::class.java))
        }



        // Toggle sound on/off.
        binding.imageSound.setOnClickListener {
            if(config.sound) {
                config.sound = false
                binding.imageSound.alpha = 0.5f
            } else {
                config.sound = true
                binding.imageSound.alpha = 1f
            }
        }

        // Toggle vibration on/off.
        binding.imageVibrate.setOnClickListener {
            if(config.vibration) {
                config.vibration = false
                binding.imageVibrate.alpha = 0.5f
            } else {
                config.vibration = true
                binding.imageVibrate.alpha = 1f
            }
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }



        // Create vibrator.
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Create sound player.
        soundPool = SoundPool(5, AudioManager.STREAM_MUSIC, 0)
        soundId = soundPool.load(this, R.raw.app_src_main_res_raw_dingv1, 1)

        // Feedback.
//        if(savedInstanceState == null) {
//            var promptView = prompt_view as DefaultLayoutPromptView
//            Amplify.getSharedInstance().promptIfReady(promptView)
//        }

        // Ads.
//        val adRequest: AdRequest
//        if (BuildConfig.DEBUG) {
//            adRequest = AdRequest.Builder()
//                .addTestDevice(getString(R.string.test_device))
//                .build()
//        } else {
//            adRequest = AdRequest.Builder().build()
//        }
//        mInterstitialAd = InterstitialAd(this)
//        mInterstitialAd.adUnitId = getString(R.string.ad_unit_id);
//        mInterstitialAd.loadAd(adRequest)
//        mInterstitialAd.adListener = object : AdListener() {
//            override fun onAdClosed() {
//                mInterstitialAd.loadAd(adRequest)
//            }
//        }

        // Create the presenter.
        BreathePresenter(this)
        presenter.start()

    }

    override fun onPause() {
        super.onPause()
        if(breathing) {
            presenter.stopBreathing()
        }
    }

    override fun onBackPressed() {
        if(breathing) {
            presenter.stopBreathing()
        } else {
            super.onBackPressed()
        }
    }

    override fun setPresenter(presenter: BreatheContract.Presenter) {
        this.presenter = presenter
    }

    override fun loadConfiguration(list: ArrayList<String>) {
        val adapter = ArrayAdapter(this, R.layout.spinner_item, list)
        binding.spinnerMinutes.adapter = adapter
    }

    override fun showConfiguration() {
        binding.layoutConfiguration.visibility = View.VISIBLE
    }

    override fun hideConfiguration() {
        binding.layoutConfiguration.visibility = View.INVISIBLE
    }

    override fun startAnimation() {
        // Breathing has started.
        breathing = true

        // Setup start and finish animation.
        startAnimator = ValueAnimator.ofFloat(1f, 0f)
        startAnimator.addUpdateListener {
            val value = it.animatedValue as Float

            binding.viewCircle.scaleX = value
            binding.viewCircle.scaleY = value
        }
        startAnimator.duration = 1000
        finishAnimator = ValueAnimator.ofFloat(0f, 1f)
        finishAnimator.addUpdateListener {
            val value = it.animatedValue as Float
            binding.viewCircle.scaleX = value
            binding.viewCircle.scaleY = value
        }
        finishAnimator.duration = 1000
        // Setup inhale, exhale bundle animation.
            inhaleAnimator = ValueAnimator.ofFloat(0f, 5f)
            inhaleAnimator.addUpdateListener {
                val value = it.animatedValue as Float
                binding.viewCircle.scaleX = value
                binding.viewCircle.scaleY = value
            }

           exhaleAnimator = ValueAnimator.ofFloat(5f, 0f)
           exhaleAnimator.addUpdateListener {
               val value = it.animatedValue as Float
               binding.viewCircle.scaleX = value
               binding.viewCircle.scaleY = value
           }

        animatorSet = AnimatorSet()
        animatorSet.play(inhaleAnimator).before(exhaleAnimator)
        animatorSet.duration = 5000

        // Start animation.
        startAnimator.start()

        // Start animator listener.
        startAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}

            override fun onAnimationEnd(p0: Animator?) {
                performNotification()
                // Start breathing cycle animation.
                if(breathing) {
                    animatorSet.start()
                }
            }

            override fun onAnimationCancel(p0: Animator?) {}

            override fun onAnimationStart(p0: Animator?) {}

        })

        // Breathing cycle animator listener.
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                if(breathing) {
                    animatorSet.start()
                } else {
                    finishAnimator.start()
                }
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}

        })

        inhaleAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                performNotification()
                binding.inhaleExhaleTv.text = Constants.INHALE
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })

        exhaleAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                performNotification()
                binding.inhaleExhaleTv.text = Constants.EXHALE
//                Toast.makeText(this@BreathActivity,"Exhale", Toast.LENGTH_SHORT).show()
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    override fun startTimer() {
        timer = object : CountDownTimer(config.minutes.toLong() * 60 * 1000, 1000) {
            override fun onFinish() {
                presenter.stopBreathing()
            }
            override fun onTick(millisUntilFinished: Long) {

            }
        }.start()
    }

    override fun stopTimer() {
        timer.cancel()
    }

    override fun stopAnimation() {
        // Stop breathing.
        breathing = false
        // Show configuration.
        showConfiguration()
        // Fonce animation end.
        startAnimator.removeAllListeners()
        startAnimator.end()
        startAnimator.cancel()
        finishAnimator.removeAllListeners()
        finishAnimator.end()
        finishAnimator.cancel()
        inhaleAnimator.removeAllListeners()
        inhaleAnimator.end()
        inhaleAnimator.cancel()
        exhaleAnimator.removeAllListeners()
        exhaleAnimator.end()
        exhaleAnimator.cancel()
        animatorSet.end()
        animatorSet.removeAllListeners()
        animatorSet.cancel()
        // Show ad.
//        showInterstitialAd()
    }

    private fun performNotification() {
        if(config.vibration) {
            performHapticFeedback()
        }
        if(config.sound) {
            performSound()
        }
    }

    private fun performHapticFeedback() {
        binding.viewCircle.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    private fun performSound() {
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }


}