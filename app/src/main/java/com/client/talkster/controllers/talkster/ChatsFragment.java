package com.client.talkster.controllers.talkster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.client.talkster.HomeActivity;
import com.client.talkster.PrivateChatActivity;
import com.client.talkster.R;
import com.client.talkster.SettingsActivity;
import com.client.talkster.adapters.ChatListAdapter;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.Chat;
import com.client.talkster.classes.Message;
import com.client.talkster.classes.User;
import com.client.talkster.classes.UserAccount;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.classes.theme.ToolbarElements;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.dto.MessageDTO;
import com.client.talkster.interfaces.IChatListener;
import com.client.talkster.interfaces.IFragmentActivity;
import com.client.talkster.interfaces.IRecyclerViewItemClickListener;
import com.client.talkster.interfaces.IThemeManagerFragmentListener;
import com.client.talkster.utils.BundleExtraNames;
import com.client.talkster.utils.FileUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatsFragment extends Fragment implements IFragmentActivity, IChatListener, NavigationView.OnNavigationItemSelectedListener, IThemeManagerFragmentListener
{

    private ToolbarElements toolbarElements;

    private float x1,x2;
    private boolean doReload = false;
    private final int MIN_DISTANCE = 300;

    private final User user;
    private final UserJWT userJWT;
    private HashMap<Long, Chat> chatHashMap;
    private ChatListAdapter chatListAdapter;

    private View rightPager;
    private RelativeLayout navBarHeader;
    private TextView userNavbarName;
    private TextView userNavbarEmail;
    private RecyclerView userChatList;
    private DrawerLayout drawerLayout;
    private ImageView userNavbarAvatar;
    private ConstraintLayout welcomeBlock;
    private NavigationView navigationView;
    private SwipeRefreshLayout chatRefreshLayout;



    public ChatsFragment(UserJWT userJWT, User user)
    {
        this.user = user;
        this.userJWT = userJWT;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        getUIElements(view);
        reloadUserChats();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(doReload){
            updateUserChats();
            System.out.println("On Resume Run");
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        doReload = true;
    }

    @Override
    public void getUIElements(View view)
    {
        View navHeader;
        ImageButton toolbarMenuIcon;

        toolbarElements = new ToolbarElements();

        chatHashMap = new HashMap<>();

        toolbarMenuIcon = view.findViewById(R.id.toolbarMenuIcon);

        rightPager = view.findViewById(R.id.rightPager);
        navigationView = view.findViewById(R.id.nav_view);
        welcomeBlock = view.findViewById(R.id.welcomeBlock);
        userChatList = view.findViewById(R.id.userChatList);
        drawerLayout = view.findViewById(R.id.chatsLayout);
        chatRefreshLayout = view.findViewById(R.id.chatRefreshLayout);

        toolbarElements.addToolbarIcon(toolbarMenuIcon);
        toolbarElements.setToolbar(view.findViewById(R.id.toolbar));
        toolbarElements.setToolbarTitle(view.findViewById(R.id.toolbarTitle));

        navHeader = navigationView.getHeaderView(0);

        navBarHeader = navHeader.findViewById(R.id.navHeader);
        userNavbarAvatar= navHeader.findViewById(R.id.userNavbarAvatar);
        userNavbarName = navHeader.findViewById(R.id.userNavbarName);
        userNavbarEmail = navHeader.findViewById(R.id.userNavbarEmail);

        navigationView.bringToFront();
        drawerLayout.setScrimColor(Color.argb(80, 0, 0, 0));
        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout.addDrawerListener(new ActionBarDrawerToggle(getActivity(), drawerLayout, R.string.open_menu, R.string.open_menu));

        UserAccount.getInstance().getUser().setAvatar(new FileUtils(userJWT).getProfilePicture(userJWT.getID()));
        userNavbarAvatar.setImageBitmap(UserAccount.getInstance().getUser().getAvatar());

        userNavbarEmail.setText(user.getMail());
        userNavbarName.setText(user.getFullName());

        initPager();


        toolbarMenuIcon.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        chatListAdapter = new ChatListAdapter(getContext(), new IRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(int position, View v)
            {
                Intent privateChatIntent = new Intent(getContext(), PrivateChatActivity.class);

                privateChatIntent.putExtra(BundleExtraNames.USER_JWT, userJWT);
                privateChatIntent.putExtra(BundleExtraNames.USER_CHAT, chatListAdapter.chatList.get(position));

                startActivity(privateChatIntent);
            }

            @Override
            public void onItemLongClick(int position, View v) {
            }
        }, new FileUtils(userJWT));
        userChatList.setAdapter(chatListAdapter);

        chatRefreshLayout.setOnRefreshListener(
                () -> {
                    reloadUserChats();
                    chatRefreshLayout.setRefreshing(false);
                }
        );
    }

    private void reloadUserChats()
    {
        APIHandler<Object, FragmentActivity> apiHandler = new APIHandler<>(getActivity());
        apiHandler.apiGET(APIEndpoints.TALKSTER_API_CHAT_GET_CHATS, userJWT.getAccessToken());
    }

    private void updateUserChats(){
        APIHandler<Object, FragmentActivity> apiHandler = new APIHandler<>(getActivity());
        apiHandler.apiGET(APIEndpoints.TALKSTER_API_CHAT_GET_CHATS_INFO, userJWT.getAccessToken());
    }

    private void updateChatListVisibility()
    {
        if(chatListAdapter.chatList == null || chatListAdapter.chatList.size() == 0)
        {
            welcomeBlock.setVisibility(View.VISIBLE);
            userChatList.setVisibility(View.INVISIBLE);
            return;
        }
        welcomeBlock.setVisibility(View.INVISIBLE);
        userChatList.setVisibility(View.VISIBLE);
    }

    private void onUserReceivedMessage(Message message)
    {
        long chatID = message.getChatID();
        APIHandler<Object, FragmentActivity> apiHandler = new APIHandler<>(getActivity());

        if(chatHashMap.containsKey(chatID))
        {
            Chat chat = chatHashMap.get(chatID);
            int chatIndex = chatListAdapter.chatList.indexOf(chat);

            switch (message.getMessageType())
            {
                case MEDIA_MESSAGE:
                case AUDIO_MESSAGE:
                case TEXT_MESSAGE:
                {

                    chat.getMessages().add(message);
                    Collections.swap(chatListAdapter.chatList, chatIndex, 0);

                    chatListAdapter.notifyItemChanged(chatIndex);
                    chatListAdapter.notifyItemMoved(chatIndex, 0);
                    break;
                }
                case CLEAR_CHAT_HISTORY:
                {
                    chat.clearMessages();
                    chatListAdapter.notifyItemChanged(chatIndex);
                    break;
                }
                case DELETE_CHAT:
                {
                    chatListAdapter.chatList.remove(chatIndex);
                    chatListAdapter.notifyItemRemoved(chatIndex);
                    break;
                }
            }

            Intent intent = new Intent(BundleExtraNames.CHAT_PRIVATE_MESSAGE_RECEIVED);

            intent.putExtra(BundleExtraNames.CHAT_ID, chatID);
            intent.putExtra(BundleExtraNames.CHAT_SEND_MESSAGE_BUNDLE, message);

            if(getActivity() != null)
                getActivity().sendBroadcast(intent);

            return;
        }

        apiHandler.apiGET(String.format(Locale.getDefault(),"%s/%d/%d", APIEndpoints.TALKSTER_API_CHAT_GET_NEW_CHAT, message.getChatID(), userJWT.getID()), userJWT.getAccessToken());
    }

    private void initPager(){
        rightPager.setOnTouchListener(new View.OnTouchListener() {
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
                        float deltaX = x1 - x2;
                        if (deltaX > MIN_DISTANCE) {
                            ((HomeActivity)getActivity()).selectNavigationButton(1);}
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void addChat(Chat chat)
    {
        chatHashMap.put(chat.getId(), chat);
        chatListAdapter.chatList.add(0, chat);

        updateChatListVisibility();

        chatListAdapter.notifyItemInserted(0);
        chatListAdapter.notifyItemChanged(0);
    }

    public void updateChat(Chat chat){
        long chatID = chat.getId();

        if (chatHashMap.containsKey(chatID)) {
            Chat oldChat = chatHashMap.get(chatID);
            int chatIndex = chatListAdapter.chatList.indexOf(oldChat);

            oldChat.setReceiverFirstname(chat.getReceiverFirstname());
            oldChat.setReceiverLastname(chat.getReceiverLastname());

            if (chat.getOwnerID() != userJWT.getID()){
                ChatListAdapter.ChatViewHolder holder = chatListAdapter.viewHashMap.get(chatID);
                FileUtils fileUtils = new FileUtils(userJWT);
                holder.userAvatarImage.setImageBitmap(fileUtils.getProfilePicture(chat.getReceiverID()));
            }

            chatListAdapter.notifyItemChanged(chatIndex);
        }
        else {
            APIHandler<Object, FragmentActivity> apiHandler = new APIHandler<>(getActivity());
            apiHandler.apiGET(APIEndpoints.TALKSTER_API_CHAT_GET_NEW_CHAT+"/"+chatID+"/"+userJWT.getID(), userJWT.getAccessToken());
        }
    }

    @Override
    public void updateChatList(List<Chat> chatList)
    {
        chatHashMap.clear();

        chatListAdapter.chatList = chatList;
        chatList.forEach(chat -> chatHashMap.put(chat.getId(), chat));
        updateChatListVisibility();

        chatListAdapter.notifyDataSetChanged();
    }

    public void updateChatListInfo(List<Chat> chatList){
        chatList.forEach(this::updateChat);
    }

    @Override
    public void onChatHistoryCleared(long chatID)
    {
        if(!chatHashMap.containsKey(chatID))
            return;

        Chat chat = chatHashMap.get(chatID);
        int chatIndex = chatListAdapter.chatList.indexOf(chat);

        chat.clearMessages();
        chatListAdapter.notifyItemChanged(chatIndex);
    }

    @Override
    public void onChatDeleted(long chatID)
    {
        if(!chatHashMap.containsKey(chatID))
            return;

        Chat chat = chatHashMap.get(chatID);
        int chatIndex = chatListAdapter.chatList.indexOf(chat);

        chatListAdapter.notifyItemRemoved(chatIndex);

        chatListAdapter.chatList.remove(chatIndex);
        chatHashMap.remove(chatID);

    }

    @Override
    public void onChatMuted(long chatID, long muteTime)
    {
        if(!chatHashMap.containsKey(chatID))
            return;

        Chat chat = chatHashMap.get(chatID);
        int chatIndex = chatListAdapter.chatList.indexOf(chat);

        chat.setMuteTime(muteTime);
        chatListAdapter.notifyItemChanged(chatIndex);
    }

    @Override
    public void onMessageReceived(String messageRAW)
    {
        ModelMapper modelMapper = new ModelMapper();
        Message message = modelMapper.map(new Gson().fromJson(messageRAW, MessageDTO.class), Message.class);

        onUserReceivedMessage(message);
    }

    @Override
    public void onSendPrivateMessage(Message message)
    {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();

        if(id == R.id.settingsItem)
        {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        }

        return false;
    }

    @Override
    public void onThemeChanged()
    {
        ThemeManager.changeToolbarColor(toolbarElements);

        userNavbarName.setTextColor(ThemeManager.getColor("navBarText"));
        userNavbarEmail.setTextColor(ThemeManager.getColor("navBarSubText"));

        navBarHeader.setBackground(ThemeManager.getThemeImage(getContext()));


        int[][] states = new int[][] { new int[] { android.R.attr.state_selected }, new int[] {  } };

        chatListAdapter.onThemeChanged();

        navigationView.setBackgroundColor(ThemeManager.getColor("navBarBackground"));
        drawerLayout.setBackgroundColor(ThemeManager.getColor("windowBackgroundWhite"));
        navigationView.setItemTextColor(new ColorStateList(states, new int[] {ThemeManager.getColor("navBarText"), ThemeManager.getColor("navBarText") }));
        navigationView.setItemIconTintList(new ColorStateList(states, new int[] {ThemeManager.getColor("navBarIcon"), ThemeManager.getColor("navBarIcon") }));
    }
}