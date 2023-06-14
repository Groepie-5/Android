package com.example.streamapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.streamapplication.models.Message;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final ArrayList<Message> messages;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final TextView messageSender;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            messageSender = (TextView) view.findViewById(R.id.message_sender);
            messageText = (TextView) view.findViewById(R.id.message_text);
        }
        public void setSender(String username) {
            if (null == messageSender) return;
            messageSender.setText(username);
        }

        public void setMessage(String message) {
            if (null == messageText) return;
            messageText.setText(message);
        }
    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView
     */
    public ChatAdapter(ArrayList<Message> dataSet) {
        messages = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.chat_message, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Message message = messages.get(position);
        viewHolder.setMessage(message.getMessageText());
        viewHolder.setSender(message.getSender());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return messages.size();
    }
}

