package com.client.talkster.controllers.talkster;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.client.talkster.R;
import com.client.talkster.interfaces.IFragmentActivity;

public class PeoplesFragment extends Fragment implements IFragmentActivity
{
    public PeoplesFragment() { }
    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_peoples, container, false);
        getUIElements(view);
        return view;
    }

    @Override
    public void getUIElements(View view)
    {

    }
}