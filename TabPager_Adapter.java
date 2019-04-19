package com.example.instantchat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class TabPager_Adapter extends FragmentPagerAdapter {
    public TabPager_Adapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int tab) {

        switch (tab){
            case 0:
                ChatFragment chatFragment = new ChatFragment();
                return  chatFragment;

            case 1:
                FriendFragment friendFragment = new FriendFragment();
                return  friendFragment;

            case 2:
                RequestFragment requestFragment = new RequestFragment();
                return  requestFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    public  CharSequence getPageTitle(int tab){

        switch (tab){
            case 0:
                return "Chats";

            case 1:
                return "Friends";

            case 2:
                return "Requests";

            default:
                return null;
        }
    }

}
