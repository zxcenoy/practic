package com.example.practica.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practica.Classes.Order;
import com.example.practica.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {
    private List<Order> orders;

    public OrdersAdapter(List<Order> orders) {
        this.orders = orders;
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDate, tvAmount, tvItems, tvAddress, tvStatus;

        OrderViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvItems = itemView.findViewById(R.id.tvItems);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        void bind(Order order) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM | hh:mm a", Locale.getDefault());
            tvDate.setText(sdf.format(order.getCreatedAt()));
            tvAmount.setText(String.format("$%.2f", order.getTotalAmount()));

            String itemsText = order.getItems().stream()
                    .map(item -> "• " + item.getName() + " x" + item.getQuantity() + " (" + String.format("$%.2f", item.getPrice() * item.getQuantity()) + ")")
                    .collect(Collectors.joining("\n"));

            tvItems.setText(itemsText);
            tvAddress.setText(order.getAddress());
            tvStatus.setText(getStatusText(order.getStatusId()));
        }

        private String getStatusText(int statusId) {
            switch (statusId) {
                case 1: return "В обработке";
                case 2: return "Готовится";
                case 3: return "В пути";
                case 4: return "Доставлен";
                default: return "Неизвестно";
            }
        }
    }
}