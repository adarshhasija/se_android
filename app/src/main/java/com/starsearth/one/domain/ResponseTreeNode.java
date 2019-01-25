package com.starsearth.one.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ResponseTreeNode implements Parcelable {

    private Response data;
    private long startTimeMillis = 0; //Start time of a set of characters forming a word
    private List<ResponseTreeNode> children;

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }

    ResponseTreeNode() {
        //used for the root, which has no data
        this.children = new ArrayList<>();
    }

    ResponseTreeNode(Response data) {
        this.data = data;
        children = new ArrayList<>();
    }

    protected ResponseTreeNode(Parcel in) {
        data = in.readParcelable(Response.class.getClassLoader());
        children = in.createTypedArrayList(ResponseTreeNode.CREATOR);
    }

    public static final Creator<ResponseTreeNode> CREATOR = new Creator<ResponseTreeNode>() {
        @Override
        public ResponseTreeNode createFromParcel(Parcel in) {
            return new ResponseTreeNode(in);
        }

        @Override
        public ResponseTreeNode[] newArray(int size) {
            return new ResponseTreeNode[size];
        }
    };

    public Response getData() {
        return data;
    }

    public void setData(Response data) {
        this.data = data;
    }

    public List<ResponseTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<ResponseTreeNode> children) {
        this.children = children;
    }

    public void addChild(ResponseTreeNode responseTreeNode) {
        this.children.add(responseTreeNode);
    }

    public void addChildren(List<ResponseTreeNode> responseTreeNodeList) {
        this.children.addAll(responseTreeNodeList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(data, i);
        parcel.writeTypedList(children);
    }
}
