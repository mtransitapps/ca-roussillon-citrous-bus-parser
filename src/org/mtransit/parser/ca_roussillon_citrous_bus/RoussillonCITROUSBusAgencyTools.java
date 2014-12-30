package org.mtransit.parser.ca_roussillon_citrous_bus;

import java.util.regex.Pattern;

import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MSpec;
import org.mtransit.parser.mt.data.MTrip;

// http://www.amt.qc.ca/developers/
// http://www.amt.qc.ca/xdata/citrous/google_transit.zip
public class RoussillonCITROUSBusAgencyTools extends DefaultAgencyTools {

	public static final String ROUTE_TYPE_FILTER = "3"; // bus only

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-roussillon-citrous-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new RoussillonCITROUSBusAgencyTools().start(args);
	}

	@Override
	public void start(String[] args) {
		System.out.printf("Generating CITROUS bus data...\n");
		long start = System.currentTimeMillis();
		super.start(args);
		System.out.printf("Generating CITROUS bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludeRoute(GRoute gRoute) {
		if (ROUTE_TYPE_FILTER != null && !gRoute.route_type.equals(ROUTE_TYPE_FILTER)) {
			return true;
		}
		return super.excludeRoute(gRoute);
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.route_long_name;
		routeLongName = MSpec.SAINT.matcher(routeLongName).replaceAll(MSpec.SAINT_REPLACEMENT);
		if (gRoute.route_short_name.equals("115-37")) {
			routeLongName = "115 " + routeLongName;
		}
		return MSpec.cleanLabel(routeLongName);
	}

	@Override
	public String getRouteShortName(GRoute gRoute) {
		if (gRoute.route_short_name.equals("T-34")) {
			return "T34";
		}
		if (gRoute.route_short_name.equals("115-37")) {
			return "37";
		}
		return super.getRouteShortName(gRoute);
	}

	private static final String ROUTE_COLOR = "6FB43F";

	@Override
	public String getRouteColor(GRoute gRoute) {
		return ROUTE_COLOR;
	}

	private static final String ROUTE_TEXT_COLOR = "FFFFFF";

	@Override
	public String getRouteTextColor(GRoute gRoute) {
		return ROUTE_TEXT_COLOR;
	}

	@Override
	public void setTripHeadsign(MRoute route, MTrip mTrip, GTrip gTrip) {
		String stationName = cleanTripHeadsign(gTrip.trip_headsign);
		int directionId = Integer.valueOf(gTrip.direction_id);
		if (mTrip.getRouteId() == 33l) {
			if (directionId == 0) {
				stationName = "Stationnement Georges-Gagné";
			} else {
				stationName = "Gare Ste-Catherine";
			}
		} else if (mTrip.getRouteId() == 210l) {
			if (directionId == 0) {
				stationName = "Delson-Longueuil-Delson";
			} else {
				stationName = "Longueuil-Delson";
			}
		}
		mTrip.setHeadsignString(stationName, directionId);
	}

	private static final Pattern DIRECTION = Pattern.compile("(direction )", Pattern.CASE_INSENSITIVE);
	private static final String DIRECTION_REPLACEMENT = "";

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = DIRECTION.matcher(tripHeadsign).replaceAll(DIRECTION_REPLACEMENT);
		return MSpec.cleanLabelFR(tripHeadsign);
	}

	private static final Pattern START_WITH_FACE_A = Pattern.compile("^(face à )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static final Pattern START_WITH_FACE_AU = Pattern.compile("^(face au )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static final Pattern START_WITH_FACE = Pattern.compile("^(face )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	private static final Pattern SPACE_FACE_A = Pattern.compile("( face à )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static final Pattern SPACE_WITH_FACE_AU = Pattern.compile("( face au )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static final Pattern SPACE_WITH_FACE = Pattern.compile("( face )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	private static final Pattern[] START_WITH_FACES = new Pattern[] { START_WITH_FACE_A, START_WITH_FACE_AU, START_WITH_FACE };

	private static final Pattern[] SPACE_FACES = new Pattern[] { SPACE_FACE_A, SPACE_WITH_FACE_AU, SPACE_WITH_FACE };

	private static final Pattern AVENUE = Pattern.compile("( avenue)", Pattern.CASE_INSENSITIVE);
	private static final String AVENUE_REPLACEMENT = " av.";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = AVENUE.matcher(gStopName).replaceAll(AVENUE_REPLACEMENT);
		gStopName = Utils.replaceAll(gStopName, START_WITH_FACES, MSpec.SPACE);
		gStopName = Utils.replaceAll(gStopName, SPACE_FACES, MSpec.SPACE);
		return super.cleanStopNameFR(gStopName);
	}

	@Override
	public int getStopId(GStop gStop) {
		return Integer.valueOf(getStopCode(gStop)); // using stop code as stop ID
	}

}
