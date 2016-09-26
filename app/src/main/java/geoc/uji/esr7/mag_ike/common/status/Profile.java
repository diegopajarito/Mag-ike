package geoc.uji.esr7.mag_ike.common.status;

/**
 * Created by pajarito on 22/09/2016.
 */

public class Profile {

    // Profile properties

    private String name;
    private int avatar;
    private int avatar_id;
    private String gender;
    private String ageRange;
    private int bikeType;
    private int bike_type_id;
    private String email;

    // Fixed Values for id's and domains

    public final String nameDefault = "Cyclist";

    public final String gender_male = "Male";
    public final String gender_female = "Female";
    public final String gender_not_set = "Not set";

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
        this.name = nameDefault;
        this.avatar_id = avatar_female_id_1;
        this.gender =gender_not_set;
        this.ageRange = age_range_not_set;
        this.bikeType = bike_type_id_1;


    }

    // Constructor for first time definition of profile
    public Profile(String name, int avatar, String gender, String age, int bike){
        this.name = name;
        this.avatar = avatar;
        this.gender = gender;
        this.bikeType = bike;

    }

    // Pending to define the way of setting id's based ond internal id from R.id.resource
    public int getAvatarId(int avatar){
        /*switch (avatar){
            case: R.id.ic_avatar_female_cyclist_0{

            }
        }*/
        return 0;
    }

    public boolean updateProfile( Profile p){
        boolean changed = false;
        if ( this.name.equals(p.getName()) == false ){
            setName(p.getName());
            changed = true;
        }
        if (this.avatar == p.avatar){
            setAvatar(p.getAvatar());
            changed = true;
        }
        if (this.gender == p.gender){
            setGender(p.getGender());
            changed = true;
        }
        if (this.ageRange.equals(p.ageRange)){
            setAgeRange(p.getAgeRange());
            changed = true;
        }
        if (this.bikeType == p.bikeType){
            setBikeType(p.getBikeType());
            changed = true;
        }

        return changed;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        // Add id set
        this.avatar = avatar;

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
