// com.project.smarthome.adapters.AutomationAdapter
package com.project.smarthome.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.project.smarthome.R;
import com.project.smarthome.models.Automation;
import java.util.List;

public class AutomationAdapter extends RecyclerView.Adapter<AutomationAdapter.ViewHolder> {

    private List<Automation> automations;
    private OnToggleListener toggleListener;

    public interface OnToggleListener {
        void onToggle(Automation automation);
    }

    public AutomationAdapter(List<Automation> automations, OnToggleListener toggleListener) {
        this.automations = automations;
        this.toggleListener = toggleListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_automation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Automation automation = automations.get(position);
        holder.bind(automation);
    }

    @Override
    public int getItemCount() {
        return automations.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvTrigger;
        private TextView tvAction;
        private Switch switchEnabled;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_automation_name);
            tvTrigger = itemView.findViewById(R.id.tv_automation_trigger);
            tvAction = itemView.findViewById(R.id.tv_automation_action);
            switchEnabled = itemView.findViewById(R.id.switch_enabled);
        }

        void bind(Automation automation) {
            tvName.setText(automation.getName());

            String triggerText = "Триггер: " + automation.getTrigger_type();
            if (automation.getTrigger_type().equals("device_state")) {
                triggerText += " - " + automation.getTrigger_value();
            } else if (automation.getTrigger_type().equals("time")) {
                triggerText += " - " + automation.getSchedule();
            }
            tvTrigger.setText(triggerText);

            tvAction.setText("Действие: " + automation.getAction());

            switchEnabled.setChecked(automation.isEnabled());

            switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (toggleListener != null) {
                    toggleListener.onToggle(automation);
                }
            });
        }
    }
}