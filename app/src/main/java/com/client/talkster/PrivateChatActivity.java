package com.client.talkster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.client.talkster.adapters.ChatMessagesAdapter;
import com.client.talkster.classes.Chat;
import com.client.talkster.classes.Message;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.dto.MessageDTO;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.utils.BundleExtraNames;
import com.client.talkster.utils.enums.MessageType;

import org.modelmapper.ModelMapper;

import java.time.OffsetDateTime;

public class PrivateChatActivity extends AppCompatActivity implements IActivity
{

    private Chat chat;
    private String CHAT_BROADCAST = BundleExtraNames.CHAT_RECEIVE_BROADCAST;
    private UserJWT userJWT;
    private TextView userNameText;
    private ImageButton chatSendButton;
    private EditText chatInputText;
    private TextView userStatusText;
    private RecyclerView chatMessagesList;
    private BroadcastReceiver messageReceiver;
    private ChatMessagesAdapter chatMessagesAdapter;
    private LinearLayoutManager recyclerLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat);

        getBundleElements();
        getUIElements();
    }

    @Override
    public void getUIElements()
    {
        userNameText = findViewById(R.id.userNameText);
        chatInputText = findViewById(R.id.chatInputText);
        chatSendButton = findViewById(R.id.chatSendButton);
        userStatusText = findViewById(R.id.userStatusText);
        chatMessagesList = findViewById(R.id.chatMessagesList);
        recyclerLayoutManager = (LinearLayoutManager)chatMessagesList.getLayoutManager();

        userNameText.setText(chat.getReceiverName());
        userStatusText.setText("last seen at 12:35");

        chatMessagesAdapter = new ChatMessagesAdapter(chat.getMessages(), userJWT.getID(), this);
        chatMessagesList.setAdapter(chatMessagesAdapter);

        chatSendButton.setOnClickListener(view ->
        {
            String message = chatInputText.getText().toString().trim();

            int length = message.length();

            if(length == 0 || length > 4097)
                return;

            MessageDTO messageDTO = new MessageDTO();

            chatInputText.setText("");
            messageDTO.setchatid(chat.getId());
            messageDTO.setmessagecontent(message);
            messageDTO.setsenderid(userJWT.getID());
            messageDTO.setjwttoken(userJWT.getJWTToken());
            messageDTO.setreceiverid(chat.getReceiverID());
            messageDTO.setmessagetype(MessageType.TEXT_MESSAGE);
            messageDTO.setmessagetimestamp(OffsetDateTime.now().toString());

            Message newMessage = new ModelMapper().map(messageDTO, Message.class);

            Intent intent = new Intent(BundleExtraNames.CHAT_SEND_MESSAGE_BROADCAST);
            intent.putExtra(BundleExtraNames.CHAT_NEW_MESSAGE, messageDTO);
            sendBroadcast(intent);
        });
    }

    private void addMessage(Message message)
    {
        chatMessagesAdapter.getMessages().add(message);
        chatMessagesAdapter.notifyItemInserted(chatMessagesAdapter.getItemCount() - 1);
        recyclerLayoutManager.scrollToPositionWithOffset(chatMessagesAdapter.getItemCount() - 1,0);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        IntentFilter filter = new IntentFilter(CHAT_BROADCAST);
        registerReceiver(messageReceiver, filter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(messageReceiver);
    }

    @Override
    public void getBundleElements()
    {
        Bundle bundle = getIntent().getExtras();

        if(bundle.isEmpty())
            return;

        chat = (Chat) bundle.get(BundleExtraNames.USER_CHAT);
        userJWT = (UserJWT) bundle.get(BundleExtraNames.USER_JWT);
        CHAT_BROADCAST += chat.getId();

        messageReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (!intent.getAction().equals(CHAT_BROADCAST))
                    return;


                Message message = (Message) intent.getExtras().get(BundleExtraNames.CHAT_NEW_MESSAGE);
                addMessage(message);
            }
        };
    }
}