package test.com.test.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import test.com.test.R;
import test.com.test.model.UserInfo;
import test.com.test.net.NetAPI;
import test.com.test.ui.MapActivity;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private ArrayList<UserInfo> mDataSet;
    private Context mContext;
    private final ImageLoader mImageLoader;

    public UserListAdapter(ArrayList<UserInfo> list, Context context) {
        mDataSet = list;
        mContext = context;
        mImageLoader = NetAPI.getInstance(mContext).getImageLoader();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_list_user, parent, false);

        return new ViewHolder(v);

    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final UserInfo item = mDataSet.get(position);
        holder.mUserName.setText(item.owner.name + " " + item.owner.surname);

        if (holder.mImageContainer != null) {
            holder.mImageContainer.cancelRequest();
        }
        holder.mImageContainer = mImageLoader.get(item.owner.foto, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                holder.mUserImage.setImageBitmap(response.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                holder.mUserImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.unknown_user));
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MapActivity.class);
                intent.putExtra(MapActivity.EXTRA_USER_ID, item.userId);

                mContext.startActivity(intent);
            }
        });
    }

    public void setDataSet(ArrayList<UserInfo> users) {
        mDataSet = users;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (mDataSet != null) ? mDataSet.size() : 0;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mUserName;
        private CircleImageView mUserImage;
        private ImageLoader.ImageContainer mImageContainer;

        ViewHolder(View itemView) {
            super(itemView);

            mUserName = (TextView) itemView.findViewById(R.id.user_name);
            mUserImage = (CircleImageView) itemView.findViewById(R.id.user_icon);
        }
    }
}

