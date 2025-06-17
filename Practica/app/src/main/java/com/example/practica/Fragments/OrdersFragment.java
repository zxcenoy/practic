package com.example.practica.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practica.Adapters.OrdersAdapter;
import com.example.practica.Classes.Order;
import com.example.practica.Managers.AuthManager;
import com.example.practica.R;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {
    private static final String ARG_STATUS = "status";
    private String statusFilter;
    private OrdersAdapter adapter;
    private AuthManager authManager;

    public static OrdersFragment newInstance(String status) {
        OrdersFragment fragment = new OrdersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authManager = new AuthManager(requireContext());

        if (getArguments() != null) {
            statusFilter = getArguments().getString(ARG_STATUS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rvOrders);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrdersAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadOrders();
    }

    private void loadOrders() {
        authManager.getOrders(getContext(), statusFilter, new AuthManager.OrderCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                requireActivity().runOnUiThread(() -> {
                    if (orders.isEmpty()) {
                        showEmptyState();
                    } else {
                        adapter.updateOrders(orders);
                    }
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    String fullError = "Loading Error: " + error +
                            statusFilter +
                            "\nUserID: " + authManager.getCurrentUserId();
                    Log.e("Loading error",fullError.toString());
                    showEmptyState( );
                });
            }
        });
    }

    private void showEmptyState() {
        String message = statusFilter == null ?
                "You don't have orders" :
                String.format("No orders with status code: %s", statusFilter);
        Log.e("",message.toString());
    }
}