package com.client.talkster.controllers.talkster;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.client.talkster.HomeActivity;
import com.client.talkster.MyApplication;
import com.client.talkster.R;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.User;
import com.client.talkster.classes.UserAccount;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.classes.theme.ButtonElements;
import com.client.talkster.classes.theme.ToolbarElements;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.dto.ChatCreateDTO;
import com.client.talkster.dto.NameDTO;
import com.client.talkster.interfaces.IFragmentActivity;
import com.client.talkster.interfaces.IThemeManagerFragmentListener;
import com.client.talkster.utils.FileUtils;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.imageview.ShapeableImageView;

public class PeoplesFragment extends Fragment implements IFragmentActivity, IThemeManagerFragmentListener
{
    private boolean FRAGMENT_CREATED = false;

    private FileUtils fileUtils;
    private ButtonElements buttonElements;
    private ToolbarElements toolbarElements;
    private ConstraintLayout peoplesLayout;

    private ShapeableImageView profileImageView;
    private View profileView, leftPager;
    private TextView firstNameView, lastNameView;
    private ImageButton lastNameEditButton, lastNameSaveButton;
    private EditText firstNameEditText, lastNameEditText, mailEditText;
    private Button galleryButton, cameraButton, addFriendButton, deleteButton;
    private ImageButton imageEditButton, closeMediaButton, firstNameEditButton, firstNameSaveButton;
    private ConstraintLayout mediaChooserLayout, firstNameLayout, firstNameEditLayout, lastNameLayout, lastNameEditLayout;

    private final int MIN_DISTANCE = 300;
    private float x1,x2;
    private final String[] PERMISSIONS = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
    };
    private final String[] ALLOWED_TYPES = {
            "image/png",
            "image/jpg",
            "image/jpeg"
    };


    public PeoplesFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        FRAGMENT_CREATED = true;
        fileUtils = new FileUtils();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_peoples, container, false);
        getUIElements(view);
        updateProfilePicture();
        return view;
    }

    @Override
    public void getUIElements(View view)
    {
        User user = UserAccount.getInstance().getUser();
        UserJWT userJWT = UserAccount.getInstance().getUserJWT();

        buttonElements = new ButtonElements();
        toolbarElements = new ToolbarElements();

        toolbarElements.setToolbar(view.findViewById(R.id.toolbar));
        toolbarElements.setToolbarTitle(view.findViewById(R.id.toolbarTitle));

        addFriendButton = view.findViewById(R.id.addFriendButton);
        peoplesLayout = view.findViewById(R.id.peoplesLayout);
        profileImageView = view.findViewById(R.id.profileImageView);
        imageEditButton = view.findViewById(R.id.imageEditButton);
        galleryButton = view.findViewById(R.id.galleryButton);
        cameraButton = view.findViewById(R.id.cameraButton);
        deleteButton = view.findViewById(R.id.deleteButton);
        closeMediaButton = view.findViewById(R.id.closeMediaButton);
        mediaChooserLayout = view.findViewById(R.id.mediaChooserLayout);
        profileView = view.findViewById(R.id.profileView);
        
        firstNameLayout = view.findViewById(R.id.firstNameLayout);
        firstNameEditLayout = view.findViewById(R.id.firstNameEditLayout);
        firstNameEditButton = view.findViewById(R.id.firstNameEditButton);
        firstNameSaveButton = view.findViewById(R.id.firstNameSaveButton);

        lastNameLayout = view.findViewById(R.id.lastNameLayout);
        lastNameEditLayout = view.findViewById(R.id.lastNameEditLayout);
        lastNameEditButton = view.findViewById(R.id.lastNameEditButton);
        lastNameSaveButton = view.findViewById(R.id.lastNameSaveButton);

        firstNameView = view.findViewById(R.id.firstNameView);
        lastNameView = view.findViewById(R.id.lastNameView);
        firstNameEditText = view.findViewById(R.id.firstNameEditText);
        lastNameEditText = view.findViewById(R.id.lastNameEditText);

        buttonElements.addButton(addFriendButton, false);
        buttonElements.addImageButton(imageEditButton, true);
        buttonElements.addImageButton(firstNameEditButton, true);
        buttonElements.addImageButton(firstNameSaveButton, true);
        buttonElements.addImageButton(lastNameEditButton, true);
        buttonElements.addImageButton(lastNameSaveButton, true);


        firstNameView.setText(user.getFirstname());
        firstNameEditText.setText(user.getFirstname());

        String lastname = user.getLastname();

        if (lastname.isBlank()){
            lastNameView.setText(R.string.last_name_placeholder);
            lastNameView.setTextColor(getResources().getColor(R.color.previewSecondaryText));
            lastNameEditText.setText("");
        }
        else {
            lastNameView.setText(lastname);
            lastNameEditText.setText(lastname);
        }

        mailEditText = view.findViewById(R.id.mailEditText);

        leftPager = view.findViewById(R.id.leftPager);

        initPager();

        addFriendButton.setOnClickListener(view1 -> {
            ChatCreateDTO chatCreateDTO = new ChatCreateDTO();
            MyApplication.hideKeyboard((AppCompatActivity) getActivity());
            APIHandler<ChatCreateDTO, FragmentActivity> apiHandler = new APIHandler<>(getActivity());
            chatCreateDTO.setSenderID(userJWT.getID());
            chatCreateDTO.setReceiverEmail(mailEditText.getText().toString());
            apiHandler.apiPOST(APIEndpoints.TALKSTER_API_CHAT_CREATE, chatCreateDTO, userJWT.getAccessToken());
            mailEditText.setText("");
        });

        imageEditButton.setOnClickListener(view1 -> {
            int permReadExt = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            int permWriteExt = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int permCamera = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);

            if (permReadExt != PackageManager.PERMISSION_GRANTED
                    && permWriteExt != PackageManager.PERMISSION_GRANTED
                    && permCamera != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, 1);
            }
            else {
                mediaChooserLayout.animate().translationY(-(mediaChooserLayout.getHeight())).setDuration(250);
            }
        });

        closeMediaButton.setOnClickListener(view1 -> {
            mediaChooserLayout.animate().translationY(0).setDuration(250);
        });

        cameraButton.setOnClickListener(view1 -> {
            mediaChooserLayout.animate().translationY(0).setDuration(250);
            ImagePicker.Companion.with(getActivity())
                    .cameraOnly()
                    .cropSquare()
                    .maxResultSize(256,256)
                    .start(101);
        });

        galleryButton.setOnClickListener(view1 -> {
            mediaChooserLayout.animate().translationY(0).setDuration(250);
            ImagePicker.Companion.with(getActivity())
                    .galleryOnly()
                    .galleryMimeTypes(ALLOWED_TYPES)
                    .cropSquare()
                    .maxResultSize(256,256)
                    .start(101);
        });

        deleteButton.setOnClickListener(view1 -> {
            mediaChooserLayout.animate().translationY(0).setDuration(250);
            APIHandler<ChatCreateDTO, FragmentActivity> apiHandler = new APIHandler<>(getActivity());
            apiHandler.apiDELETE(APIEndpoints.TALKSTER_API_FILE_DELETE_PROFILE, userJWT.getAccessToken());
        });

        firstNameEditButton.setOnClickListener(view1 -> {
            firstNameLayout.setVisibility(View.INVISIBLE);
            firstNameEditLayout.setVisibility(View.VISIBLE);
        });

        firstNameSaveButton.setOnClickListener(view1 -> {
            updateUserName();
            firstNameLayout.setVisibility(View.VISIBLE);
            firstNameEditLayout.setVisibility(View.INVISIBLE);
        });

        lastNameEditButton.setOnClickListener(view1 -> {
            lastNameLayout.setVisibility(View.INVISIBLE);
            lastNameEditLayout.setVisibility(View.VISIBLE);
        });

        lastNameSaveButton.setOnClickListener(view1 -> {
            updateUserName();
            lastNameLayout.setVisibility(View.VISIBLE);
            lastNameEditLayout.setVisibility(View.INVISIBLE);
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

    private void initPager(){
        leftPager.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                // ... Respond to touch events
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        float deltaX = x2 - x1;
                        if (deltaX > MIN_DISTANCE) {
                            ((HomeActivity)getActivity()).selectNavigationButton(1);}
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        updateProfilePicture();
    }

    @Override
    public void onThemeChanged()
    {
        if(!FRAGMENT_CREATED)
            return;

        ThemeManager.changeButtonsColor(buttonElements);
        ThemeManager.changeToolbarColor(toolbarElements);

        peoplesLayout.setBackgroundColor(ThemeManager.getColor("windowBackgroundWhite"));
    }
    
    public void updateProfilePicture() {
        Bitmap profile = fileUtils.getProfilePicture(UserAccount.getInstance().getUser().getId());
        profileImageView.setImageBitmap(profile);
        UserAccount.getInstance().getUser().setAvatar(profile);
    }

    private void updateUserName()
    {
        User user = UserAccount.getInstance().getUser();

        MyApplication.hideKeyboard((AppCompatActivity) getActivity());
        String first = firstNameEditText.getText().toString();
        String last = lastNameEditText.getText().toString();
        if (first.isBlank()){
            Toast.makeText(getContext(), "First name can't be empty!", Toast.LENGTH_LONG).show();
            firstNameEditText.setText(firstNameView.getText());
            return;
        }
        user.setFirstname(first);
        user.setLastname(last);
        firstNameView.setText(first);
        if (!last.isBlank()){
            lastNameView.setText(last);
            lastNameView.setTextColor(getResources().getColor(R.color.previewMainText));
        } else {
            lastNameView.setText(R.string.last_name_placeholder);
            lastNameView.setTextColor(getResources().getColor(R.color.previewSecondaryText));
            lastNameEditText.setText("");
        }
        NameDTO nameDTO = new NameDTO();
        APIHandler<NameDTO, FragmentActivity> apiHandler = new APIHandler<>(getActivity());
        nameDTO.setFirstName(first);
        nameDTO.setLastName(last);
        apiHandler.apiPUT(APIEndpoints.TALKSTER_API_USER_UPDATE_NAME, nameDTO, UserAccount.getInstance().getUserJWT().getAccessToken());
    }
}