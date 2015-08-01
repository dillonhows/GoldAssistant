package com.parttime.addresslist.userdetail;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parttime.net.DefaultCallback;
import com.parttime.net.HuanXinRequest;
import com.parttime.net.UserDetailRequest;
import com.parttime.pojo.UserDetailVO;
import com.qingmu.jianzhidaren.BuildConfig;
import com.qingmu.jianzhidaren.R;
import com.quark.model.HuanxinUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 *
 * Created by dehua on 15/7/28.
 */
public class UserDetailPagerAdapter extends FragmentPagerAdapter {

    public ArrayList<String> userIds;
    public HashMap<String, UserDetailVO> cache = new HashMap<>();
    public UserDetailActivity.FromAndStatus fromAndStatus;

    public UserDetailActivity userDetailActivity;

    public UserDetailPagerAdapter(FragmentManager fm, UserDetailActivity userDetailActivity) {
        super(fm);
        this.userDetailActivity = userDetailActivity;
    }

    public void setData(LinkedHashSet<String> userIds) {

        this.userIds = new ArrayList<>(userIds);

    }

    @Override
    public Fragment getItem(int position) {
        String userId = userIds.get(position);
        userDetailActivity.initUserBlock(userId);
        UserDetailFragment fragment = UserDetailFragment.newInstance(userId);
        fragment.userDetailPagerAdapter = this;
        return fragment;
    }

    @Override
    public int getCount() {
        return userIds.size();
    }

    public static class UserDetailFragment extends Fragment {
        private static final String ARG_USER_ID = "userId";
        protected String userId;
        UserDetailPagerAdapter userDetailPagerAdapter;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static UserDetailFragment newInstance(String userId) {
            UserDetailFragment fragment = new UserDetailFragment();
            Bundle args = new Bundle();
            args.putString(ARG_USER_ID, userId);
            fragment.setArguments(args);
            return fragment;
        }

        public UserDetailFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            userId = getArguments().getString(ARG_USER_ID);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.activity_user_detail_item, container, false);
            final UserDetailViewHelper helper = new UserDetailViewHelper(this, userDetailPagerAdapter);
            UserDetailViewHelper.InitContent initContent = null;
            if (BuildConfig.DEBUG) { //for test
                userDetailPagerAdapter.fromAndStatus = UserDetailActivity.FromAndStatus.FROM_NORMAL_GROUP_AND_IS_FRIEND;
            }
            if (UserDetailActivity.FromAndStatus.FROM_NORMAL_GROUP_AND_NOT_FRIEND == userDetailPagerAdapter.fromAndStatus) {
                initContent = UserDetailViewHelper.InitContent.INIT_FRIEND;
            } else if (UserDetailActivity.FromAndStatus.FROM_NORMAL_GROUP_AND_IS_FRIEND == userDetailPagerAdapter.fromAndStatus) {
                initContent = UserDetailViewHelper.InitContent.INIT_FRIEND;
            } else if (UserDetailActivity.FromAndStatus.FROM_ACTIVITY_GROUP_AND_NOT_FINISH == userDetailPagerAdapter.fromAndStatus) {
                initContent = UserDetailViewHelper.InitContent.INIT_RESUME;
            } else if (UserDetailActivity.FromAndStatus.FROM_ACTIVITY_GROUP_AND_IS_FINISH == userDetailPagerAdapter.fromAndStatus) {
                initContent = UserDetailViewHelper.InitContent.INIT_APPRAISE;
            }
            helper.initView(rootView, initContent);

            final UserDetailViewHelper.InitContent initContent2 = initContent;
            if (UserDetailActivity.FromAndStatus.FROM_ACTIVITY_GROUP_AND_NOT_FINISH == userDetailPagerAdapter.fromAndStatus ||
                    UserDetailActivity.FromAndStatus.FROM_ACTIVITY_GROUP_AND_IS_FINISH == userDetailPagerAdapter.fromAndStatus) {
                UserDetailVO userDetailVO = userDetailPagerAdapter.cache.get(userId);
                if (userDetailVO != null) {
                    helper.reflesh(userDetailVO, initContent);
                } else {
                    new UserDetailRequest().getUserDetail(userId,
                            userDetailPagerAdapter.userDetailActivity.groupId,
                            userDetailPagerAdapter.userDetailActivity.queue,
                            new DefaultCallback() {
                                @Override
                                public void success(Object obj) {
                                    if (obj != null && obj instanceof UserDetailVO) {
                                        UserDetailVO vo = (UserDetailVO) obj;
                                        userDetailPagerAdapter.cache.put(userId, vo);
                                        helper.reflesh(vo, initContent2);
                                    }
                                }
                            });
                }
            } else if (UserDetailActivity.FromAndStatus.FROM_NORMAL_GROUP_AND_NOT_FRIEND == userDetailPagerAdapter.fromAndStatus ||
                    UserDetailActivity.FromAndStatus.FROM_NORMAL_GROUP_AND_IS_FRIEND == userDetailPagerAdapter.fromAndStatus) {
                initHuanxinUserData(userId, helper, initContent2);
            }
            return rootView;
        }

        private void initHuanxinUserData(String userId, final UserDetailViewHelper helper, final UserDetailViewHelper.InitContent initContent) {


            if (userId != null) {
                UserDetailVO userDetailVO = userDetailPagerAdapter.cache.get(userId);
                if(userDetailVO != null){
                    helper.reflesh(userDetailVO, initContent);
                }else {
                    /*StringBuilder userIdsStr = new StringBuilder();
                    int size = userIds.size();
                    for (int i = 0; i < size; i++) {
                        if(i < size -1 ) {
                            userIdsStr.append(userIds.get(i)).append(",");
                        }else{
                            userIdsStr.append(userIds.get(i));
                        }
                    }*/
                    new HuanXinRequest().getHuanxinUserDetailList(userId, userDetailPagerAdapter.userDetailActivity.queue, new DefaultCallback() {
                        @Override
                        public void success(Object obj) {
                            super.success(obj);
                            if (obj instanceof ArrayList) {
                                @SuppressLint("Unchecked")
                                ArrayList<HuanxinUser> list = (ArrayList<HuanxinUser>) obj;
                                if (list.size() == 1) {
                                    for (HuanxinUser huanxinUser : list) {
                                        UserDetailVO userDetailVO = new UserDetailVO();
                                        userDetailVO.userId = huanxinUser.getUid();
                                        userDetailVO.name = huanxinUser.getName();
                                        userDetailVO.picture_1 = huanxinUser.getAvatar();
                                        userDetailVO.sex = huanxinUser.sex;
                                        userDetailVO.creditworthiness = huanxinUser.creditworthiness;
                                        userDetailVO.earnest_money = huanxinUser.earnest_money;
                                        userDetailVO.certification = huanxinUser.certification;
                                        userDetailPagerAdapter.cache.put(huanxinUser.getUid(), userDetailVO);
                                        helper.reflesh(userDetailVO, initContent);
                                    }
                                }
                            }
                        }
                    });
                }
            }
        }
    }
}
