package com.client.talkster.controllers.talkster;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.client.talkster.R;
import com.client.talkster.api.APIStompWebSocket;
import com.client.talkster.classes.User;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.interfaces.IFragmentActivity;
import com.client.talkster.interfaces.IThemeManagerActivityListener;
import com.client.talkster.interfaces.IThemeManagerFragmentListener;
import com.client.talkster.utils.FileUtils;
import com.github.dhaval2404.imagepicker.ImagePicker;

public class PeoplesFragment extends Fragment implements IFragmentActivity, IThemeManagerFragmentListener
{
    private boolean test = false;
    private UserJWT userJWT;
    private User user;
    private EditText receiverInput;
    private Button sendMessageButton, galleryButton, cameraButton;
    private ImageButton imageEditButton, closeMediaButton;
    private ImageView profileImageView;
    private EditText sendMessageInput;
    private View profileView;
    private ConstraintLayout mediaChooserLayout;
    private FileUtils fileUtils;
    public APIStompWebSocket apiStompWebSocket;

    public PeoplesFragment(UserJWT userJWT, User user) { this.userJWT = userJWT; this.user = user;}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileUtils = new FileUtils(userJWT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_peoples, container, false);
        getUIElements(view);
        setProfilePicture(fileUtils.getProfilePicture());
        return view;
    }

    @Override
    public void getUIElements(View view)
    {
        receiverInput = view.findViewById(R.id.receiverInput);
        sendMessageInput = view.findViewById(R.id.sendMessageInput);
        sendMessageButton = view.findViewById(R.id.sendMessageButton);
        profileImageView = view.findViewById(R.id.profileImageView);
        imageEditButton = view.findViewById(R.id.imageEditButton);
        galleryButton = view.findViewById(R.id.galleryButton);
        cameraButton = view.findViewById(R.id.cameraButton);
        closeMediaButton = view.findViewById(R.id.closeMediaButton);
        mediaChooserLayout = view.findViewById(R.id.mediaChooserLayout);
        profileView = view.findViewById(R.id.profileView);

        sendMessageButton.setOnClickListener(view1 -> {

            /*MessageDTO messageDTO = new MessageDTO();

            messageDTO.setsenderid(userJWT.getID());
            messageDTO.setjwttoken(userJWT.getAccessToken());
            messageDTO.setmessagetype(MessageType.TEXT_MESSAGE);
            messageDTO.setmessagecontent(sendMessageInput.getText().toString());

            if(receiverInput.getText().toString().length() > 0)
                messageDTO.setreceiverid(Long.parseLong(receiverInput.getText().toString()));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                messageDTO.setmessagetimestamp(OffsetDateTime.now().toString());

            if(receiverInput.getText().toString().length() == 0)
                apiStompWebSocket.getWebSocketClient().send("/app/message", new Gson().toJson(messageDTO)).subscribe();
            else
                apiStompWebSocket.getWebSocketClient().send("/app/private-message", new Gson().toJson(messageDTO)).subscribe();*/

//            getActivity().setTheme(R.style.Theme_Talkster_First);
//            getActivity().recreate();
//            getActivity().setContentView(R.layout.fragment_peoples);

            if(!test)
            {
                ThemeManager.applyTheme(R.style.Theme_Talkster_Second);
                test = true;
            }
            else
            {
                ThemeManager.applyTheme(R.style.Theme_Talkster_First);
                test = false;
            }
        });

        imageEditButton.setOnClickListener(view1 -> {
            mediaChooserLayout.animate().translationY(-(mediaChooserLayout.getHeight())).setDuration(250);
        });

        closeMediaButton.setOnClickListener(view1 -> {
            mediaChooserLayout.animate().translationY(0).setDuration(250);
            System.out.println("close button pushed");
        });

        cameraButton.setOnClickListener(view1 -> {
            mediaChooserLayout.animate().translationY(0).setDuration(250);
            ImagePicker.Companion.with(PeoplesFragment.this)
                    .cameraOnly()
                    .cropSquare()
                    .maxResultSize(256,256)
                    .start(101);
        });

        galleryButton.setOnClickListener(view1 -> {
            mediaChooserLayout.animate().translationY(0).setDuration(250);
            ImagePicker.Companion.with(PeoplesFragment.this)
                    .galleryOnly()
                    .cropSquare()
                    .maxResultSize(256,256)
                    .start(101);
        });

        profileView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                // ... Respond to touch events
                mediaChooserLayout.animate().translationY(0).setDuration(250);
                return false;
            }
        });
    }

    public void setProfilePicture(Bitmap bitmap){
        profileImageView.setImageBitmap(bitmap);
    }

    @Override
    public void onThemeChanged()
    {
        getActivity().setTheme(ThemeManager.getCurrentTheme());
        ThemeManager.reloadThemeColors(getContext());

        Context context = getContext();
//        Drawable buttonGradient = AppCompatResources.getDrawable(context, R.drawable.drawable_button_gradient);

        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{
                ThemeManager.getColor("button_BackgroundGradient1"),
                ThemeManager.getColor("button_BackgroundGradient2"),
                ThemeManager.getColor("button_BackgroundGradient3")
        });

        float dip = 5f;
        Resources r = getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );

        gradientDrawable.setCornerRadius(px);

        sendMessageButton.setBackground(gradientDrawable);

    }
}