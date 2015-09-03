package com.robotemplates.cityguide.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.robotemplates.cityguide.R;


public class AboutDialogFragment extends DialogFragment
{
	public static AboutDialogFragment newInstance()
	{
		AboutDialogFragment fragment = new AboutDialogFragment();
		return fragment;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setCancelable(true);
		setRetainInstance(true);
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		// cancelable on touch outside
		if(getDialog()!=null) getDialog().setCanceledOnTouchOutside(true);
	}
	
	
	@Override
	public void onDestroyView()
	{
		// http://code.google.com/p/android/issues/detail?id=17423
		if(getDialog() != null && getRetainInstance()) getDialog().setDismissMessage(null);
		super.onDestroyView();
	}
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getActivity());

		builder
		.setTitle(R.string.dialog_about_title)
		.setMessage(Html.fromHtml(getResources().getString(R.string.dialog_about_message)))
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				// do nothing
			}
		});
		
		return builder.create();
	}
}
