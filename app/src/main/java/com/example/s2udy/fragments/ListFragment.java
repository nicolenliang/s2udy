package com.example.s2udy.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.s2udy.R;
import com.example.s2udy.adapters.ListAdapter;
import com.example.s2udy.models.ListItem;
import com.example.s2udy.models.Room;
import com.example.s2udy.models.User;
import com.google.android.material.tabs.TabLayout;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class ListFragment extends Fragment
{
    public static final String TAG = "ListFragment";
    Room room;
    List<ListItem> items;
    ListAdapter adapter;
    CardView cvList;
    TextView tvTitle;
    EditText etItem, etEdit;
    RecyclerView rvList;
    Button btnAdd, btnClear;
    User user = (User) User.getCurrentUser();

    public ListFragment(Room room)
    {
        this.room = room;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        items = new ArrayList<>();

        cvList = view.findViewById(R.id.cvList);
        tvTitle = view.findViewById(R.id.tvTitle);
        etItem = view.findViewById(R.id.etItem);
        rvList = view.findViewById(R.id.rvList);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnClear = view.findViewById(R.id.btnClear);
        RelativeLayout rlItem = view.findViewById(R.id.rlItem);

        Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_up);
        cvList.startAnimation(bottomUp);

        TabLayout tabLayout = requireActivity().findViewById(R.id.tabLayout);
        CircleIndicator3 indicator = requireActivity().findViewById(R.id.indicator);
        // keyboard listener: if up, disappear tablayout
        KeyboardVisibilityEvent.setEventListener(requireActivity(), new KeyboardVisibilityEventListener()
        {
            @Override
            public void onVisibilityChanged(boolean isOpen)
            {
                if (isOpen)
                {
                    tabLayout.setVisibility(View.GONE);
                    indicator.setVisibility(View.GONE);
                    ViewGroup.LayoutParams rvParams = rvList.getLayoutParams();
                    rvParams.height = rvList.getHeight() - (3 * etItem.getHeight());
                    rvList.setLayoutParams(rvParams);

                    FrameLayout.LayoutParams rlParams = (FrameLayout.LayoutParams) rlItem.getLayoutParams();
                    rlParams.setMargins(0, 0, 0, etItem.getHeight() + 10);
                    rlItem.setLayoutParams(rlParams);
                }
                else // keyboard back down
                {
                    tabLayout.setVisibility(View.VISIBLE);
                    indicator.setVisibility(View.VISIBLE);
                    ViewGroup.LayoutParams rvParams = rvList.getLayoutParams();
                    rvParams.height = rvList.getHeight() + (3 * etItem.getHeight());
                    rvList.setLayoutParams(rvParams);

                    FrameLayout.LayoutParams rlParams = (FrameLayout.LayoutParams) rlItem.getLayoutParams();
                    rlParams.setMargins(0, 0, 0, 2 * etItem.getHeight() + 20);
                    rlItem.setLayoutParams(rlParams);
                }
            }
        });
        // editing/deleting items from list
        ListAdapter.onLongClickListener longClickListener = new ListAdapter.onLongClickListener()
        {
            @Override
            public void onItemLongClicked(int position)
            {
                View editDialog = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_list, null);
                etEdit = editDialog.findViewById(R.id.etItem);
                etEdit.setText(items.get(position).getBody());

                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle("edit item")
                        .setView(editDialog)
                        .setPositiveButton("save", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Log.i(TAG,"old item: " + items.get(position).getBody());
                                String newBody = etEdit.getText().toString();
                                ListItem editItem = items.get(position);
                                editItem(editItem, newBody);
                                Log.i(TAG, "new item: " + items.get(position).getBody());
                            }
                        })
                        .setNegativeButton("delete", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                deleteItem(items.get(position));
                                adapter.notifyItemRemoved(position);
                            }
                        })
                        .create();
                dialog.show();
            }
        };
        // checking off items on list
        ListAdapter.onCheckedChangeListener checkedChangeListener = new ListAdapter.onCheckedChangeListener()
        {
            @Override
            public void onItemCheckedChange(int position)
            {
                ListItem checkItem = items.get(position);
                Log.i(TAG, "clicked position " + position + "; checkItem: " + checkItem.getDone().toString());
                updateCheck(checkItem);
            }
        };

        adapter = new ListAdapter(getContext(), items, longClickListener, checkedChangeListener);
        rvList.setAdapter(adapter);
        rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        loadItems();

        btnAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String body = etItem.getText().toString();
                if (body.isEmpty())
                {
                    Toast.makeText(getContext(), "cannot add empty item!", Toast.LENGTH_SHORT).show();
                    return;
                }
                ListItem newItem = new ListItem();
                saveItem(newItem, body);
                rvList.smoothScrollToPosition(0);
            }
        });
        btnClear.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                clearItems();
            }
        });
    }

    private void loadItems()
    {
        ParseQuery<ListItem> query = ParseQuery.getQuery(ListItem.class);
        query.findInBackground(new FindCallback<ListItem>()
        {
            @Override
            public void done(List<ListItem> objects, ParseException e)
            {
                if (e != null)
                {
                    Log.e(TAG, "loadItems() done error: ", e);
                    return;
                }
                for (ListItem item : objects)
                {
                    if (!items.contains(item) && item.getRoom().hasSameId(room))
                        items.add(item);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void saveItem(ListItem newItem, String body)
    {
        newItem.setBody(body);
        newItem.setRoom(room);
        newItem.setColor(user.getColor());
        newItem.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if (e != null)
                {
                    Log.e(TAG, "saveItem() error in saving: ", e);
                    return;
                }
                items.add(newItem);
                adapter.notifyItemInserted(items.size() - 1);
                Log.i(TAG, "saveItem() successful");
            }
        });
        etItem.setText("");
    }

    private void editItem(ListItem editItem, String newBody)
    {
        editItem.setBody(newBody);
        editItem.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if (e != null)
                {
                    Log.e(TAG, "editItem() error in saving: ", e);
                    return;
                }
                Log.i(TAG, "editItem() successful");
                adapter.notifyDataSetChanged();
            }
        });
        etEdit.setText("");
    }

    private void deleteItem(ListItem delItem)
    {
        ParseQuery<ListItem> query = ParseQuery.getQuery(ListItem.class);
        query.getInBackground(delItem.getObjectId(), new GetCallback<ListItem>()
        {
            @Override
            public void done(ListItem object, ParseException e)
            {
                if (e != null)
                    return;
                object.deleteInBackground();
                Log.i(TAG, "deleteItem() successful");
            }
        });
        items.remove(delItem);
    }

    private void clearItems()
    {
        adapter.clear();
        ParseQuery<ListItem> query = ParseQuery.getQuery(ListItem.class);
        query.findInBackground(new FindCallback<ListItem>()
        {
            @Override
            public void done(List<ListItem> objects, ParseException e)
            {
                if (e != null)
                {
                    Log.e(TAG, "clearItems() error in fetching: ", e);
                    return;
                }
                for (ListItem item : objects)
                {
                    if (item.getRoom().hasSameId(room))
                    {
                        item.deleteInBackground(new DeleteCallback()
                        {
                            @Override
                            public void done(ParseException e)
                            {
                                if (e != null)
                                    return;
                                Log.i(TAG, "clearItems() delete successful");
                            }
                        });
                    }
                }
            }
        });
    }

    private void updateCheck(ListItem checkItem)
    {
        boolean done = checkItem.getDone();
        checkItem.setDone(!done);
        checkItem.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if (e != null)
                {
                    Log.e(TAG, "updateCheck() error in saving: ", e);
                    return;
                }
                Log.i(TAG, "updateCheck() successful");
            }
        });
    }
}