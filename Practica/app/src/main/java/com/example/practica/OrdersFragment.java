package com.example.practica;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practica.AuthManager;
import com.example.practica.Order;
import com.example.practica.OrdersAdapter;
import com.example.practica.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
                    String fullError = "Ошибка загрузки: " + error +
                            "\nФильтр: " + statusFilter +
                            "\nUserID: " + authManager.getCurrentUserId();
                    Toast.makeText(getContext(), fullError, Toast.LENGTH_LONG).show();
                    showEmptyState( );
                });
            }
        });
    }

    private void showEmptyState() {
        String message = statusFilter == null ?
                "У вас пока нет заказов" :
                String.format("Нет заказов со статусом: %s", statusFilter);

        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}