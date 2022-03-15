package realestatescraper.util;

public class DistrictConverter {

    public static String getTwoDigitDistrict(int d) {
        d = (d % 1000) / 10;
        if (d < 10) {
            return "0" + d;
        } else {
            return "" + d;
        }
    }

    public static String getName(int d) {
        switch (d) {
            case 1010:
                return "Innere Stadt";
            case 1020:
                return "Leopoldstadt";
            case 1030:
                return "Landstraße";
            case 1040:
                return "Wieden";
            case 1050:
                return "Margareten";
            case 1060:
                return "Mariahilf";
            case 1070:
                return "Neubau";
            case 1080:
                return "Josefstadt";
            case 1090:
                return "Alsergrund";
            case 1100:
                return "Favoriten";
            case 1110:
                return "Simmering";
            case 1120:
                return "Meidling";
            case 1130:
                return "Hietzing";
            case 1140:
                return "Penzing";
            case 1150:
                return "Rudolfsheim-Fünfhaus";
            case 1160:
                return "Ottakring";
            case 1170:
                return "Hernals";
            case 1180:
                return "Währing";
            case 1190:
                return "Döbling";
            case 1200:
                return "Brigittenau";
            case 1210:
                return "Floridsdorf";
            case 1220:
                return "Donaustadt";
            case 1230:
                return "Liesing";
            default:
                return null;
        }
    }
}
