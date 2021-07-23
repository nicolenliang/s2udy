package com.example.s2udy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.s2udy.R;
import com.example.s2udy.models.ListItem;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter
{
    Context context;
    List<ListItem> items;
    onLongClickListener longClickListener;
    onCheckedChangeListener checkedChangeListener;

    public interface onLongClickListener
    {
        void onItemLongClicked(int position);
    }

    public interface onCheckedChangeListener
    {
        void onItemCheckedChange(int position);
    }

    public ListAdapter(Context context, List<ListItem> items, onLongClickListener longClickListener, onCheckedChangeListener checkedChangeListener)
    {
        this.context = context;
        this.items = items;
        this.longClickListener = longClickListener;
        this.checkedChangeListener = checkedChangeListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        ListItem item = items.get(position);
        ((ViewHolder)holder).bind(item);
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    public void clear()
    {
        items.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        CheckBox cbItem;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            cbItem = itemView.findViewById(R.id.cbItem);
        }

        public void bind(ListItem item)
        {
            cbItem.setText(item.getBody());
            cbItem.setChecked(item.getDone());

            cbItem.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    longClickListener.onItemLongClicked(getAdapterPosition());
                    return true;
                }
            });
            cbItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    checkedChangeListener.onItemCheckedChange(getAdapterPosition());
                }
            });
        }
    }
}
