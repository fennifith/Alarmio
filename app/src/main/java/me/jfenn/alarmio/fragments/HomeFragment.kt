package me.jfenn.alarmio.fragments

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.afollestad.aesthetic.Aesthetic
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import io.reactivex.disposables.Disposable
import jahirfiquitiva.libs.fabsmenu.FABsMenu
import jahirfiquitiva.libs.fabsmenu.FABsMenuListener
import jahirfiquitiva.libs.fabsmenu.TitleFAB
import me.jfenn.alarmio.Alarmio
import me.jfenn.alarmio.R
import me.jfenn.alarmio.adapters.SimplePagerAdapter
import me.jfenn.alarmio.data.AlarmData
import me.jfenn.alarmio.data.PreferenceData
import me.jfenn.alarmio.dialogs.AestheticTimeSheetPickerDialog
import me.jfenn.alarmio.dialogs.TimerDialog
import me.jfenn.alarmio.interfaces.FragmentInstantiator
import me.jfenn.alarmio.utils.ImageUtils
import me.jfenn.alarmio.views.PageIndicatorView
import me.jfenn.androidutils.bind
import me.jfenn.androidutils.getStatusBarHeight
import me.jfenn.timedatepickers.dialogs.PickerDialog
import me.jfenn.timedatepickers.views.LinearTimePickerView
import java.util.*

class HomeFragment : BaseFragment() {

    private val viewPager: ViewPager? by bind(R.id.viewPager)
    private val tabLayout: TabLayout? by bind(R.id.tabLayout)
    private val timePager: ViewPager? by bind(R.id.timePager)
    private val timeIndicator: PageIndicatorView? by bind(R.id.pageIndicator)
    private val bottomSheet: View? by bind(R.id.bottomSheet)
    private val background: ImageView? by bind(R.id.background)
    private val overlay: View? by bind(R.id.overlay)
    private val menu: FABsMenu? by bind(R.id.fabsMenu)
    private val stopwatchFab: TitleFAB? by bind(R.id.stopwatchFab)
    private val timerFab: TitleFAB? by bind(R.id.timerFab)
    private val alarmFab: TitleFAB? by bind(R.id.alarmFab)

    private val behavior: BottomSheetBehavior<*> by lazy { BottomSheetBehavior.from(bottomSheet!!) }
    private var shouldCollapseBack = false

    private var colorPrimarySubscription: Disposable? = null
    private var colorAccentSubscription: Disposable? = null
    private var textColorPrimarySubscription: Disposable? = null
    private var textColorPrimaryInverseSubscription: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        behavior.isHideable = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                private var statusBarHeight = -1
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_COLLAPSED) bottomSheet.setPadding(0, 0, 0, 0) else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        if (statusBarHeight < 0) statusBarHeight = bottomSheet.context.getStatusBarHeight()
                        bottomSheet.setPadding(0, statusBarHeight, 0, 0)
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (statusBarHeight < 0) statusBarHeight = bottomSheet.context.getStatusBarHeight()
                    bottomSheet.setPadding(0, (slideOffset * statusBarHeight).toInt(), 0, 0)
                }
            })
        }

        viewPager?.adapter = SimplePagerAdapter(
                context, childFragmentManager,
                AlarmsFragment.Instantiator(context),
                SettingsFragment.Instantiator(context)
        )

        tabLayout?.setupWithViewPager(viewPager)
        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position > 0) {
                    shouldCollapseBack = behavior.state != BottomSheetBehavior.STATE_EXPANDED
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    menu?.hide()
                } else {
                    setClockFragments()
                    menu?.show()
                    if (shouldCollapseBack) {
                        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        shouldCollapseBack = false
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        setClockFragments()

        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                behavior.peekHeight = view.measuredHeight / 2
                view.findViewById<View>(R.id.timeContainer).layoutParams = CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, view.measuredHeight / 2)
            }
        })

        colorPrimarySubscription = Aesthetic.get()
                .colorPrimary()
                .subscribe { integer: Int ->
                    bottomSheet?.setBackgroundColor(integer)
                    overlay?.setBackgroundColor(integer)
                }
        colorAccentSubscription = Aesthetic.get()
                .colorAccent()
                .subscribe { integer: Int ->
                    menu?.menuButtonColor = integer
                    val color: Int = ContextCompat.getColor(context!!, if (PreferenceData.THEME.getValue<Int>(context, Alarmio.THEME_DAY_NIGHT) == Alarmio.THEME_AMOLED) R.color.textColorPrimary else R.color.textColorPrimaryNight)
                    menu?.menuButton?.setColorFilter(color)
                    stopwatchFab?.setColorFilter(color)
                    timerFab?.setColorFilter(color)
                    alarmFab?.setColorFilter(color)
                    stopwatchFab?.setBackgroundColor(integer)
                    timerFab?.setBackgroundColor(integer)
                    alarmFab?.setBackgroundColor(integer)
                }
        textColorPrimarySubscription = Aesthetic.get()
                .textColorPrimary()
                .subscribe { integer: Int ->
                    stopwatchFab?.titleTextColor = integer
                    timerFab?.titleTextColor = integer
                    alarmFab?.titleTextColor = integer
                }
        textColorPrimaryInverseSubscription = Aesthetic.get()
                .textColorPrimaryInverse()
                .subscribe { integer: Int ->
                    alarmFab?.titleBackgroundColor = integer
                    stopwatchFab?.titleBackgroundColor = integer
                    timerFab?.titleBackgroundColor = integer
                }

        stopwatchFab?.setOnClickListener { view: View? ->
            menu?.collapseImmediately()
            fragmentManager!!.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_up_sheet, R.anim.slide_out_up_sheet, R.anim.slide_in_down_sheet, R.anim.slide_out_down_sheet)
                    .replace(R.id.fragment, StopwatchFragment())
                    .addToBackStack(null)
                    .commit()
        }

        timerFab?.setOnClickListener { view: View? ->
            invokeTimerScheduler()
            menu?.collapse()
        }

        alarmFab?.setOnClickListener { view: View? ->
            invokeAlarmScheduler()
            menu?.collapse()
        }

        menu?.menuListener = object : FABsMenuListener() {
            override fun onMenuExpanded(fabsMenu: FABsMenu) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) requestPermissions(arrayOf(Manifest.permission.FOREGROUND_SERVICE), 0) else fabsMenu.collapseImmediately()
                }
            }
        }

        // check actions passed from MainActivity; open timer/alarm schedulers if necessary
        val action: String? = arguments?.getString(INTENT_ACTION, null)
        if (AlarmClock.ACTION_SET_ALARM == action) {
            view.post { invokeAlarmScheduler() }
        } else if (AlarmClock.ACTION_SET_TIMER == action) {
            view.post { invokeTimerScheduler() }
        }
    }

    /**
     * Open the alarm scheduler dialog to allow the user to create
     * a new alarm.
     */
    private fun invokeAlarmScheduler() {
        AestheticTimeSheetPickerDialog(view!!.context)
                .setListener(object : PickerDialog.OnSelectedListener<LinearTimePickerView> {
                    override fun onSelect(dialog: PickerDialog<LinearTimePickerView>, view: LinearTimePickerView) {
                        val manager: AlarmManager = view.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        val alarm: AlarmData = alarmio!!.newAlarm()
                        alarm.time.set(Calendar.HOUR_OF_DAY, view.hourOfDay)
                        alarm.time.set(Calendar.MINUTE, view.minute)
                        alarm.setTime(alarmio, manager, alarm.time.timeInMillis)
                        alarm.setEnabled(context, manager, true)
                        alarmio!!.onAlarmsChanged()
                    }

                    override fun onCancel(dialog: PickerDialog<LinearTimePickerView>) {}
                })
                .show()
    }

    /**
     * Open the timer scheduler dialog to allow the user to start
     * a timer.
     */
    private fun invokeTimerScheduler() {
        TimerDialog(context, fragmentManager).show()
    }

    /**
     * Update the time zones displayed in the clock fragments pager.
     */
    private fun setClockFragments() {
        val fragments: MutableList<FragmentInstantiator> = ArrayList<FragmentInstantiator>()
        fragments.add(ClockFragment.Instantiator(context, null))
        for (id in TimeZone.getAvailableIDs()) {
            if (PreferenceData.TIME_ZONE_ENABLED.getSpecificValue<Boolean>(context, id)) fragments.add(ClockFragment.Instantiator(context, id))
        }

        val timeAdapter = SimplePagerAdapter(context, childFragmentManager, *fragments.toTypedArray())
        timePager?.adapter = timeAdapter
        timeIndicator?.setViewPager(timePager)
        timeIndicator?.visibility = if (fragments.size > 1) View.VISIBLE else View.GONE

        ImageUtils.getBackgroundImage(background)
    }

    override fun onDestroyView() {
        colorPrimarySubscription?.dispose()
        colorAccentSubscription?.dispose()
        textColorPrimarySubscription?.dispose()
        textColorPrimaryInverseSubscription?.dispose()
        timeIndicator?.unsubscribe()
        super.onDestroyView()
    }

    companion object {
        const val INTENT_ACTION = "me.jfenn.alarmio.HomeFragment.INTENT_ACTION"
    }
}