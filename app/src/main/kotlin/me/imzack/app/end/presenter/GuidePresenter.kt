package me.imzack.app.end.presenter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.View

import me.imzack.app.end.R
import me.imzack.app.end.event.PlanCreatedEvent
import me.imzack.app.end.event.TypeCreatedEvent
import me.imzack.app.end.model.DataManager
import me.imzack.app.end.model.bean.Plan
import me.imzack.app.end.model.bean.Type
import me.imzack.app.end.util.ColorUtil
import me.imzack.app.end.util.ResourceUtil
import me.imzack.app.end.view.adapter.GuidePagerAdapter
import me.imzack.app.end.view.contract.GuideViewContract
import me.imzack.app.end.view.fragment.SimpleGuidePageFragment

import org.greenrobot.eventbus.EventBus

import javax.inject.Inject

class GuidePresenter @Inject constructor(
        private var mGuideViewContract: GuideViewContract?,
        fragmentManager: FragmentManager,
        private val mEventBus: EventBus
) : BasePresenter() {

    private val mGuidePagerAdapter = GuidePagerAdapter(fragmentManager, mutableListOf(
            //欢迎页
            SimpleGuidePageFragment.Builder()
                    .setImage(R.drawable.img_logo_with_bg)
                    .setTitle(R.string.title_guide_page_welcome)
                    .setDescription(R.string.text_slogan)
                    .setButton(R.string.button_start, object : SimpleGuidePageFragment.OnButtonClickListener {
                        override fun onClick(v: View) {
                            endGuide(true)
                        }
                    })
                    .create() as Fragment
//            //引导结束页
//            SimpleGuidePageFragment.Builder()
//                    .setImage(R.drawable.ic_check_black_24dp)
//                    .setTitle(R.string.title_guide_page_ready)
//                    .setDescription(R.string.dscpt_guide_page_ready)
//                    .create()
    ))
    private var mLastBackKeyPressedTime = 0L

    override fun attach() {
        mGuideViewContract!!.showInitialView(mGuidePagerAdapter)
    }

    override fun detach() {
        mGuideViewContract = null
    }

    fun notifyNavigationButtonClicked(isLastPageButton: Boolean, currentPage: Int) {
        if (isLastPageButton) {
            if (currentPage != 0) {
                mGuideViewContract!!.navigateToPage(currentPage - 1)
            }
        } else {
            if (currentPage != mGuidePagerAdapter.count - 1) {
                //不是最后一页
                mGuideViewContract!!.navigateToPage(currentPage + 1)
            } else {
                //最后一页
                endGuide(true)
            }
        }
    }

    fun notifyPageSelected(selectedPage: Int) {
        mGuideViewContract!!.onPageSelected(selectedPage == 0, selectedPage == mGuidePagerAdapter.count - 1)
    }

    fun notifyBackPressed() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - mLastBackKeyPressedTime < 1500) {
            endGuide(false)
        } else {
            mLastBackKeyPressedTime = currentTime
            mGuideViewContract!!.showToast(R.string.toast_double_press_exit)
        }
    }

    private fun createDefaultData() {
        //Default types
        val nameResIds = intArrayOf(R.string.def_type_1, R.string.def_type_2, R.string.def_type_3, R.string.def_type_4)
        val colorResIds = intArrayOf(
                R.color.indigo,
                R.color.red,
                R.color.orange,
                R.color.green
        )
        val patternFns = arrayOf("ic_computer_black_24dp", "ic_home_black_24dp", "ic_work_black_24dp", "ic_school_black_24dp")
        for (i in 0..3) {
            val type = Type(
                    name = ResourceUtil.getString(nameResIds[i]),
                    markColor = ColorUtil.parseColor(ResourceUtil.getColor(colorResIds[i])),
                    markPattern = patternFns[i]
            )
            DataManager.notifyTypeCreated(type)
            mEventBus.post(TypeCreatedEvent(presenterName, type.code, DataManager.recentlyCreatedTypeLocation))
        }

        //Default plans
        val contentResIds = intArrayOf(R.string.def_plan_4, R.string.def_plan_3, R.string.def_plan_2, R.string.def_plan_1)
        val defaultTypeCode = DataManager.getType(0).code
        for (i in 0..3) {
            val plan = Plan(
                    content = ResourceUtil.getString(contentResIds[i]),
                    typeCode = defaultTypeCode
            )
            DataManager.notifyPlanCreated(plan)
            mEventBus.post(PlanCreatedEvent(presenterName, plan.code, DataManager.recentlyCreatedPlanLocation))
        }
    }

    private fun endGuide(isNormally: Boolean) {
        if (isNormally) {
            createDefaultData()
            DataManager.preferenceHelper.needGuideValue = false
        }
        mGuideViewContract!!.exitWithResult(isNormally)
    }
}