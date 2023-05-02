package com.client.talkster.controllers.talkster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.client.talkster.GroupChatActivity;
import com.client.talkster.activities.CreateGroupActivity;
import com.client.talkster.HomeActivity;
import com.client.talkster.PrivateChatActivity;
import com.client.talkster.R;
import com.client.talkster.SettingsActivity;
import com.client.talkster.adapters.ChatListAdapter;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.chat.Chat;
import com.client.talkster.classes.chat.GroupChat;
import com.client.talkster.classes.chat.message.Message;
import com.client.talkster.classes.chat.PrivateChat;
import com.client.talkster.classes.User;
import com.client.talkster.classes.UserAccount;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.classes.theme.ToolbarElements;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.dto.MessageDTO;
import com.client.talkster.interfaces.chat.IChatListener;
import com.client.talkster.interfaces.IFragmentActivity;
import com.client.talkster.interfaces.IRecyclerViewItemClickListener;
import com.client.talkster.interfaces.chat.IGroupChatListener;
import com.client.talkster.interfaces.theme.IThemeManagerFragmentListener;
import com.client.talkster.utils.BundleExtraNames;
import com.client.talkster.utils.FileUtils;
import com.client.talkster.utils.enums.EChatType;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatsFragment extends Fragment implements IFragmentActivity, IChatListener, NavigationView.OnNavigationItemSelectedListener, IThemeManagerFragmentListener, IGroupChatListener
{
    private HashMap<Long, GroupChat> groupChatHashMap;
    private HashMap<Long, PrivateChat> privateChatHashMap;

    private ToolbarElements toolbarElements;

    private float x1,x2;
//    private boolean doReload = false;
    private final int MIN_DISTANCE = 300;

    private View rightPager;
    private TextView userNavbarName;
    private TextView userNavbarEmail;
    private RecyclerView userChatList;
    private DrawerLayout drawerLayout;
    private ImageView userNavbarAvatar;
    private ConstraintLayout navBarHeader;
    private ConstraintLayout welcomeBlock;
    private NavigationView navigationView;
    private ChatListAdapter chatListAdapter;
    private SwipeRefreshLayout chatRefreshLayout;



    public ChatsFragment() { }

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
        userNavbarAvatar.setImageBitmap(UserAccount.getInstance().getUser().getAvatar());
//        if(doReload){
//            updateUserChats();
//            System.out.println("On Resume Run");
//        }
    }

//    @Override
//    public void onPause(){
//        super.onPause();
//        doReload = true;
//    }

    @Override
    public void getUIElements(View view)
    {
        View navHeader;
        ImageButton toolbarMenuIcon;

        User user = UserAccount.getInstance().getUser();
        UserJWT userJWT = UserAccount.getInstance().getUserJWT();

        groupChatHashMap = new HashMap<>();
        privateChatHashMap = new HashMap<>();
        toolbarElements = new ToolbarElements();

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

        UserAccount.getInstance().getUser().setAvatar(new FileUtils().getProfilePicture(userJWT.getID()));
        userNavbarAvatar.setImageBitmap(UserAccount.getInstance().getUser().getAvatar());

        userNavbarEmail.setText(user.getMail());
        userNavbarName.setText(user.getFullName());

        initPager();


        toolbarMenuIcon.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        chatListAdapter = new ChatListAdapter(getContext(), new IRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(int position, View v)
            {
                Intent intent;
                Chat chat = chatListAdapter.chatList.get(position);

                if(chat.getType() == EChatType.GROUP_CHAT)
                    intent = new Intent(getContext(), GroupChatActivity.class);

                else if(chat.getType() == EChatType.PRIVATE_CHAT)
                    intent = new Intent(getContext(), PrivateChatActivity.class);

                else
                    return;

                intent.putExtra(BundleExtraNames.USER_CHAT, chat);

                startActivity(intent);
            }

            @Override
            public void onItemLongClick(int position, View v) {
            }
        }, new FileUtils());
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
        apiHandler.apiGET(APIEndpoints.TALKSTER_API_CHAT_GET_CHATS, UserAccount.getInstance().getUserJWT().getAccessToken());
    }

    private void updateUserChats()
    {
        APIHandler<Object, FragmentActivity> apiHandler = new APIHandler<>(getActivity());
        apiHandler.apiGET(APIEndpoints.TALKSTER_API_CHAT_GET_CHATS_INFO, UserAccount.getInstance().getUserJWT().getAccessToken());
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

    private void onUserReceivedMessage(Message message, EChatType type)
    {
        Chat chat;
        long chatID = message.getChatID();
        UserJWT userJWT = UserAccount.getInstance().getUserJWT();

        if(type == EChatType.GROUP_CHAT)
            chat = groupChatHashMap.getOrDefault(chatID, null);
        else if(type == EChatType.PRIVATE_CHAT)
            chat = privateChatHashMap.getOrDefault(chatID, null);
        else
            return;

        if(chat == null)
        {
            if(type == EChatType.GROUP_CHAT)
                return;

            APIHandler<Object, FragmentActivity> apiHandler = new APIHandler<>(getActivity());
            apiHandler.apiGET(String.format(Locale.getDefault(),"%s/%d/%d", APIEndpoints.TALKSTER_API_CHAT_GET_NEW_CHAT, message.getChatID(), userJWT.getID()), userJWT.getAccessToken());

            return;
        }

        int chatIndex = chatListAdapter.chatList.indexOf(chat);

        switch (message.getMessageType())
        {
            case MEDIA_MESSAGE:
                case AUDIO_MESSAGE:
                    case TEXT_MESSAGE:
                {

                    Log.e("Debug", chat.toString());
                    chat.getMessages().add(message);
                    Log.e("Debug", chat.toString());

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

        if(chat.getType() == EChatType.GROUP_CHAT)
            groupChatHashMap.put(chat.getId(), (GroupChat) chat);
        else if(chat.getType() == EChatType.PRIVATE_CHAT)
            privateChatHashMap.put(chat.getId(), (PrivateChat) chat);
        else
            return;

        chatListAdapter.chatList.add(0, chat);

        updateChatListVisibility();

        chatListAdapter.notifyItemInserted(0);
        chatListAdapter.notifyItemChanged(0);
    }

    public void updateChat(Chat chat)
    {
        long chatID = chat.getId();
        UserJWT userJWT = UserAccount.getInstance().getUserJWT();

        Chat oldChat = getChatByID(chatID, chat.getType());

        if(oldChat == null)
        {
            APIHandler<Object, FragmentActivity> apiHandler = new APIHandler<>(getActivity());
            apiHandler.apiGET(APIEndpoints.TALKSTER_API_CHAT_GET_NEW_CHAT+"/"+chatID+"/"+userJWT.getID(), userJWT.getAccessToken());

            return;
        }

        int chatIndex = chatListAdapter.chatList.indexOf(oldChat);

        if(oldChat.getType() == EChatType.PRIVATE_CHAT)
        {
            PrivateChat oldPrivateChat = (PrivateChat)oldChat;

            oldPrivateChat.setReceiverFirstname(((PrivateChat)chat).getReceiverFirstname());
            oldPrivateChat.setReceiverLastname(((PrivateChat)chat).getReceiverLastname());

            if (((PrivateChat)chat).getOwnerID() != userJWT.getID())
            {
                ChatListAdapter.ChatViewHolder holder = chatListAdapter.viewHashMap.get(chatID);
                FileUtils fileUtils = new FileUtils();
                holder.userAvatarImage.setImageBitmap(fileUtils.getProfilePicture(((PrivateChat)chat).getReceiverID()));
            }
        }

            chatListAdapter.notifyItemChanged(chatIndex);
    }

    @Override
    public void updateChatList(List<PrivateChat> privateChats, List<GroupChat> groupChats)
    {
        clearChatHashmaps();

        chatListAdapter.chatList.clear();
        chatListAdapter.chatList.addAll(privateChats);
        chatListAdapter.chatList.addAll(groupChats);

        chatListAdapter.chatList.sort((firstChat, secondChat) -> {

            if(firstChat.getLastMessage() == null || secondChat.getLastMessage() == null)
                return 0;

            return firstChat.getLastMessage().getMessageTimestamp().compareTo(secondChat.getLastMessage().getMessageTimestamp());
        });

        Collections.reverse(chatListAdapter.chatList);

        groupChats.forEach(chat -> groupChatHashMap.put(chat.getId(), chat));
        privateChats.forEach(chat -> privateChatHashMap.put(chat.getId(), chat));

        updateChatListVisibility();

        chatListAdapter.notifyDataSetChanged();
    }

    public void updateChatListInfo(List<Chat> chatList)
    {
        chatList.forEach(this::updateChat);
    }

    @Override
    public void onChatHistoryCleared(long chatID, EChatType chatType)
    {
        Chat chat = getChatByID(chatID, chatType);

        if(chat == null)
            return;

        chat.clearMessages();
        chatListAdapter.notifyItemChanged(chatListAdapter.chatList.indexOf(chat));
    }

    @Override
    public void onChatDeleted(long chatID, EChatType chatType)
    {
        Chat chat = getChatByID(chatID, chatType);

        if(chat == null)
            return;

        int chatIndex = chatListAdapter.chatList.indexOf(chat);

        chatListAdapter.notifyItemRemoved(chatIndex);
        chatListAdapter.chatList.remove(chatIndex);

        removeChatByID(chatID, chatType);
    }

    @Override
    public void onChatMuted(long chatID, long muteTime, EChatType chatType)
    {
        Chat chat = getChatByID(chatID, chatType);

        if(chat == null)
            return;

        chat.setMuteTime(muteTime);
        chatListAdapter.notifyItemChanged(chatListAdapter.chatList.indexOf(chat));
    }

    @Override
    public void onMessageReceived(String messageRAW)
    {
        ModelMapper modelMapper = new ModelMapper();
        Message message = modelMapper.map(new Gson().fromJson(messageRAW, MessageDTO.class), Message.class);

        onUserReceivedMessage(message, EChatType.PRIVATE_CHAT);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        Intent intent = null;
        int id = item.getItemId();

        if(id == R.id.settingsItem)
            intent = new Intent(getActivity(), SettingsActivity.class);

        else if(id == R.id.newGroupItem)
            intent = new Intent(getActivity(), CreateGroupActivity.class);

        else
            return false;

        startActivity(intent);
        drawerLayout.closeDrawer(GravityCompat.START);
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

    @Override
    public void onGroupChatCreated(long groupChatID)
    {

    }

    @Override
    public void onGroupChatMessageReceived(String messageRAW)
    {
        ModelMapper modelMapper = new ModelMapper();
        Message message = modelMapper.map(new Gson().fromJson(messageRAW, MessageDTO.class), Message.class);

        onUserReceivedMessage(message, EChatType.GROUP_CHAT);
    }

    private void clearChatHashmaps()
    {
        groupChatHashMap.clear();
        privateChatHashMap.clear();
    }

    private Chat getChatByID(long chatID, EChatType chatType)
    {

        if (chatType == EChatType.PRIVATE_CHAT)
            return privateChatHashMap.get(chatID);

        else if (chatType == EChatType.GROUP_CHAT)
            return groupChatHashMap.get(chatID);

        return null;
    }

    private void removeChatByID(long chatID, EChatType chatType)
    {
        if (chatType == EChatType.PRIVATE_CHAT)
            privateChatHashMap.remove(chatID);

        else if (chatType == EChatType.GROUP_CHAT)
            groupChatHashMap.remove(chatID);
    }

}