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
import android.widget.TextView;
import android.widget.Toast;

import com.example.s2udy.R;
import com.example.s2udy.adapters.ListAdapter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class ListFragment extends Fragment
{
    public static final String TAG = "ListFragment";
    List<String> items;
    ListAdapter adapter;
    CardView cvList;
    TextView tvTitle;
    EditText etItem;
    RecyclerView rvList;
    Button btnAdd, btnClear;

    public ListFragment() {}

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

        Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_up);
        Animation bottomDown = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_down);
        cvList.startAnimation(bottomUp);

        loadItems();

        ListAdapter.onLongClickListener longClickListener = new ListAdapter.onLongClickListener()
        {
            @Override
            public void onItemLongClicked(int position)
            {
                View editDialog = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_list, null);
                EditText etItem = editDialog.findViewById(R.id.etItem);
                etItem.setText(items.get(position));

                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle("edit item")
                        .setView(editDialog)
                        .setPositiveButton("save", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Log.i(TAG,"old item: " + items.get(position));
                                items.remove(position);
                                items.add(position, etItem.getText().toString());
                                adapter.notifyDataSetChanged();
                                saveItems();
                                Log.i(TAG, "new item: " + items.get(position));
                            }
                        })
                        .setNegativeButton("delete", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                items.remove(position);
                                adapter.notifyItemRemoved(position);
                                saveItems();
                            }
                        })
                        .create();
                dialog.show();
            }
        };

        adapter = new ListAdapter(getContext(), items, longClickListener);
        rvList.setAdapter(adapter);
        rvList.setLayoutManager(new LinearLayoutManager(getContext()));

        btnAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String item = etItem.getText().toString();
                if (item.isEmpty())
                {
                    Toast.makeText(getContext(), "cannot add empty item!", Toast.LENGTH_SHORT).show();
                    return;
                }
                items.add(item);
                adapter.notifyItemInserted(items.size() - 1);
                etItem.setText("");
                saveItems();
            }
        });
        btnClear.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                adapter.clear();
                clearItems();
            }
        });
    }

    private File getDataFile()
    {
        // requireContext() used rather than getContext() bc it is a fragment; context may be null
        return new File(requireContext().getFilesDir(), "list.txt");
    }

    private void loadItems()
    {
        try
        {
            items = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
        }
        catch (IOException e)
        {
            Log.e(TAG, "error in loading list: ", e);
            items = new ArrayList<>();
        }
    }

    private void saveItems()
    {
        try
        {
            FileUtils.writeLines(getDataFile(), items);
        }
        catch (IOException e)
        {
            Log.e(TAG, "error in saving list: ", e);
        }
    }

    private void clearItems()
    {
        FileUtils.deleteQuietly(getDataFile());
    }
}