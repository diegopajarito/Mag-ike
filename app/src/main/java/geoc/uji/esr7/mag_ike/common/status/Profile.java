package geoc.uji.esr7.mag_ike.common.status;

import android.graphics.drawable.Drawable;
import android.widget.Switch;

/**
 * Created by pajarito on 22/09/2016.
 */

public class Profile {

    // Profile properties

    private String avatarName;
    private int avatarId;
    private String gender;
    private String ageRange;
    private int bikeType;
    private boolean bikeRented;
    private String email;

    // Fixed Values for id's and domains

    public final String nameDefault = "Cyclist";

    public final String gender_male = "Male";
    public final String gender_female = "Female";
    public final String text_not_set = "Not set";

    public final int id_not_set= -1;
    private final int avatar_female_id_1= 100;
    private final int avatar_female_id_2= 101;
    private final int avatar_female_id_3= 102;
    private final int avatar_female_id_4= 103;
    private final int avatar_female_id_5= 104;

    private final int avatar_male_id_1= 200;
    private final int avatar_male_id_2= 201;
    private final int avatar_male_id_3= 202;
    private final int avatar_male_id_4= 203;
    private final int avatar_male_id_5= 204;

    private final String age_range_0_20= "0-20";
    private final String age_range_20_30= "20-30";
    private final String age_range_30_40= "30-40";
    private final String age_range_40_50= "40-50";
    private final String age_range_50_0= "50+";

    private final int bike_type_id_1 = 1;
    private final int bike_type_id_2 = 2;
    private final int bike_type_id_3 = 3;
    private final int bike_type_id_4 = 4;
    private final int bike_type_id_5 = 5;
    private final int bike_type_id_6 = 6;
    private final int bike_type_id_7 = 7;
    private final int bike_type_id_8 = 8;
    private final int bike_type_id_9 = 9;
    private final int bike_type_id_10 = 10;

    private final boolean bike_rented_yes = true;
    private final boolean bike_rented_no = false;


    // Default constructor
    public Profile(){
        this.avatarName = nameDefault;
        this.avatarId = id_not_set;
        this.gender =text_not_set;
        this.ageRange = text_not_set;
        this.bikeType = id_not_set;
        this.bikeRented = bike_rented_no;
        this.email = text_not_set;


    }

    // Constructor for first time definition of profile
    public Profile(String avatarName, int avatarId, String gender, String age, int bike, String email){
        this.avatarName = avatarName;
        this.avatarId = avatarId;
        this.gender = gender;
        this.ageRange = age;
        this.bikeType = bike;
        this.email = email;
    }


    public boolean updateProfile( Profile p){
        boolean changed = false;
        if ( this.avatarName.equals(p.getAvatarName()) == false ){
            setAvatarName(p.getAvatarName());
            changed = true;
        }
        if (this.avatarId != p.avatarId){
            setAvatarId(p.getAvatarId());
            changed = true;
        }
        if (this.gender.equals(p.gender) == false){
            setGender(p.getGender());
            changed = true;
        }
        if (this.ageRange.equals(p.ageRange) == false){
            setAgeRange(p.getAgeRange());
            changed = true;
        }
        if (this.bikeType != p.bikeType){
            setBikeType(p.getBikeType());
            changed = true;
        }
        if (this.email.equals(p.email) == false){
            setEmail(p.getEmail());
            changed = true;
        }

        return changed;
    }


    public String getAvatarName() {
        return this.avatarName;
    }

    public void setAvatarName(String avatarName) {
        this.avatarName = avatarName;
    }

    public int getAvatarId() {
        int pos = this.id_not_set;
        switch (this.avatarId){
            case avatar_female_id_1:
                pos = 0;
                break;
            case avatar_male_id_1:
                pos = 1;
                break;
            case avatar_female_id_2:
                pos = 2;
                break;
            case avatar_male_id_2:
                pos = 3;
                break;
            case avatar_female_id_3:
                pos = 4;
                break;
            case avatar_male_id_3:
                pos = 5;
                break;
            case avatar_female_id_4:
                pos = 6;
                break;
            case avatar_male_id_4:
                pos = 7;
                break;
        }
        return pos;
    }

    public void setAvatarId(int avatarId) {
        // Add id set
        switch (avatarId){
            case 0:
                this.avatarId = this.avatar_female_id_1;
                break;
            case 1:
                this.avatarId = this.avatar_male_id_1;
                break;
            case 2:
                this.avatarId = this.avatar_female_id_2;
                break;
            case 3:
                this.avatarId = this.avatar_male_id_2;
                break;
            case 4:
                this.avatarId = this.avatar_female_id_3;
                break;
            case 5:
                this.avatarId = this.avatar_male_id_3;
                break;
            case 6:
                this.avatarId = this.avatar_female_id_4;
                break;
            case 7:
                this.avatarId = this.avatar_male_id_4;
                break;
            default:
                this.avatarId = this.id_not_set;
                break;
        }
    }

    public String getGender() { return gender; }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAgeRange() {
        return ageRange;
    }

    public int getAgeRangeId() {
        switch (this.ageRange){
            case age_range_0_20:
                return 0;
            case age_range_20_30:
                return 1;
            case age_range_30_40:
                return 2;
            case age_range_40_50:
                return 3;
            case age_range_50_0:
                return 4;
            default:
                return 2;
        }
    }

    public void setAgeRange(String ageRange) {
        this.ageRange = ageRange;
    }

    public void setAgeRangeById(int id){
        if (id == 0)
            this.ageRange = age_range_0_20;
        else if (id == 1)
            this.ageRange = age_range_20_30;
        else if (id == 2)
            this.ageRange = age_range_30_40;
        else if (id == 3)
            this.ageRange = age_range_40_50;
        else if (id == 4)
            this.ageRange = age_range_50_0;
        else
            this.ageRange = text_not_set;
    }

    public int getBikeType() {
        return bikeType;
    }

    public void setBikeType(int bikeType) {
        // Add id set
        this.bikeType = bikeType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isBikeRented() {
        return bikeRented;
    }

    public void setBikeRented(boolean bikeRented) {
        if (bikeRented = true)
            this.bikeRented = bike_rented_yes;
        else
            this.bikeRented = bike_rented_no;
    }
}
