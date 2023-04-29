package com.client.talkster.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.client.talkster.R;
import com.client.talkster.adapters.CreateGroupAdapter;
import com.client.talkster.classes.User;
import com.client.talkster.classes.UserAccount;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.interfaces.IRecyclerViewItemClickListener;
import com.client.talkster.utils.BundleExtraNames;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupActivity extends AppCompatActivity implements IActivity
{

    private List<User> contacts;
    private ArrayList<User> selectedContacts;
    private CreateGroupAdapter createGroupAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setTheme(ThemeManager.getCurrentThemeStyle());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        getUIElements();
    }

    @Override
    public void getUIElements()
    {
        RecyclerView contactsRecyclerView;
        ImageButton toolbarBackIcon, continueButton;

        selectedContacts = new ArrayList<>();
        contacts = UserAccount.getInstance().getUser().getContacts();

        continueButton = findViewById(R.id.continueButton);
        toolbarBackIcon = findViewById(R.id.toolbarBackIcon);
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);


        createGroupAdapter = new CreateGroupAdapter(this, contacts, new IRecyclerViewItemClickListener()
        {
            @Override
            public void onItemClick(int position, View v)
            {
                User user = contacts.get(position);

                if(selectedContacts.contains(user))
                {
                    selectedContacts.remove(user);
                    createGroupAdapter.changeContactState(position, false);

                    return;
                }
                selectedContacts.add(user);
                createGroupAdapter.changeContactState(position, true);
            }

            @Override
            public void onItemLongClick(int position, View v)
            {

            }
        });

        toolbarBackIcon.setOnClickListener(v -> finish());
        contactsRecyclerView.setAdapter(createGroupAdapter);

        continueButton.setOnClickListener(v ->
        {
            if(selectedContacts.size() == 0)
                return;

            Intent intent = new Intent(this, CreateGroupSettingsActivity.class);
            intent.putExtra(BundleExtraNames.CREATE_GROUP_MEMBER_LIST, selectedContacts);
            startActivityForResult(intent, 1);
        });

        createGroupAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (!(requestCode == 1 && resultCode == RESULT_OK))
            return;

        finish();
    }

    @Override
    public void getBundleElements()
    {

    }
}