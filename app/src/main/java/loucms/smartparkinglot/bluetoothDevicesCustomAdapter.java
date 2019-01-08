package loucms.smartparkinglot;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by loucms on 11/24/17.
 */

public class bluetoothDevicesCustomAdapter extends BaseAdapter {
    public final static String TAG = "bluetoothCustomAdapter";
    public LayoutInflater inflater;
    public List<BluetoothDevice> list;
    public Context mContext;
    public bluetoothDevicesCustomAdapter(Context context, ArrayList<BluetoothDevice> list){
        this.list = list;
        inflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public int getCount() {
        Log.d(TAG,"getCount phase");

        return list.size();
    }

    @Override
    public Object getItem(int position) {
        Log.d(TAG,"getItem phase");
        return list.get(position);
    }
    public class ViewHolder{
        TextView txtName;
        TextView txtAddress;
        circleImageView view;

        public ViewHolder(TextView txtName,TextView txtAddress,circleImageView view){
            this.txtName = txtName;
            this.txtAddress = txtAddress;
            this.view = view;

        }
    }

    @Override
    public long getItemId(int position) {
        Log.d(TAG,"getItemId phase");
        return list.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG,"getView phase");
        ViewHolder holder= null;
        if(convertView==null){
            Log.d(TAG,"convertView is null");
            convertView = inflater.inflate(R.layout.bluetooth_devices_info,null);
            holder = new ViewHolder((TextView) convertView.findViewById(R.id.deviceName),
                    (TextView) convertView.findViewById(R.id.deviceAddr),
                    (circleImageView)convertView.findViewById(R.id.image));
            convertView.setTag(holder);
        }else{
            Log.d(TAG,"convertView isn't null");
            holder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice device = (BluetoothDevice)getItem(position);
        holder.txtName.setText(device.getName());
        holder.txtAddress.setText(device.getAddress());

        String name = "ic_"+device.getName();
        int id = mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());
        holder.view.setImageResource(id);
        return convertView;
    }
    @Nullable
    @Override
    public CharSequence[] getAutofillOptions() {
        return new CharSequence[0];
    }
}
