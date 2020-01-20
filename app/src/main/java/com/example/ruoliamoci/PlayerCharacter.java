package com.example.ruoliamoci;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class PlayerCharacter implements Parcelable {
    String name;
    String classs;
    String race;
    Uri uriPhoto;
    int pos;


    PlayerCharacter(String name, String classs, String race, Uri uriPhoto,int pos) {
        this.name = name;
        this.classs = classs;
        this.race = race;
        this.uriPhoto = uriPhoto;
        this.pos=pos;
    }


    protected PlayerCharacter(Parcel in) {
        name = in.readString();
        classs = in.readString();
        race = in.readString();
        uriPhoto = in.readParcelable(Uri.class.getClassLoader());
        pos=in.readInt();
    }

    public static final Creator<PlayerCharacter> CREATOR = new Creator<PlayerCharacter>() {
        @Override
        public PlayerCharacter createFromParcel(Parcel in) {
            return new PlayerCharacter(in);
        }

        @Override
        public PlayerCharacter[] newArray(int size) {
            return new PlayerCharacter[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(classs);
        dest.writeString(race);
        dest.writeParcelable(uriPhoto, flags);
        dest.writeInt(pos);
    }
}
