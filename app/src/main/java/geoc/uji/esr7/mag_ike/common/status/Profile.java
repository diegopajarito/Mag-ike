package geoc.uji.esr7.mag_ike.common.status;

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
    private int bikeTypeId;
    private String email;

    // Fixed Values for id's and domains

    public final String nameDefault = "Cyclist";

    public final String gender_male = "Male";
    public final String gender_female = "Female";
    public final String text_not_set = "Not set";

    private final int id_not_set= -1;
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

    private final String age_range_not_set= "Not Set";
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




    // Default constructor
    public Profile(){
        this.avatarName = nameDefault;
        this.avatarId = id_not_set;
        this.gender =text_not_set;
        this.ageRange = age_range_not_set;
        this.bikeType = id_not_set;
        this.bikeTypeId = id_not_set;
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
        return this.avatarId;
    }

    public void setAvatarId(int avatarId) {
        // Add id set
        this.avatarId = avatarId;

    }

    public String getGender() { return gender; }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAgeRange() {
        return ageRange;
    }

    public void setAgeRange(String ageRange) {
        this.ageRange = ageRange;
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
}
