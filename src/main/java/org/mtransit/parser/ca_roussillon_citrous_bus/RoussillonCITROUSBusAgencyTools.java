package org.mtransit.parser.ca_roussillon_citrous_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// https://exo.quebec/en/about/open-data
// https://exo.quebec/xdata/citrous/google_transit.zip
public class RoussillonCITROUSBusAgencyTools extends DefaultAgencyTools {

	public static void main(@Nullable String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-roussillon-citrous-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new RoussillonCITROUSBusAgencyTools().start(args);
	}

	@Nullable
	private HashSet<Integer> serviceIdInts;

	@Override
	public void start(@NotNull String[] args) {
		MTLog.log("Generating CITROUS bus data...");
		long start = System.currentTimeMillis();
		this.serviceIdInts = extractUsefulServiceIdInts(args, this, true);
		super.start(args);
		MTLog.log("Generating CITROUS bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIdInts != null && this.serviceIdInts.isEmpty();
	}

	@Override
	public boolean excludeCalendar(@NotNull GCalendar gCalendar) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarInt(gCalendar, this.serviceIdInts);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(@NotNull GCalendarDate gCalendarDates) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarDateInt(gCalendarDates, this.serviceIdInts);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		if (this.serviceIdInts != null) {
			return excludeUselessTripInt(gTrip, this.serviceIdInts);
		}
		return super.excludeTrip(gTrip);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final Pattern DIGITS = Pattern.compile("[\\d]+");

	private static final String T = "T";

	private static final long RID_STARTS_WITH_T = 20_000L;

	@Override
	public long getRouteId(@NotNull GRoute gRoute) {
		if (!Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			Matcher matcher = DIGITS.matcher(gRoute.getRouteShortName());
			if (matcher.find()) {
				int digits = Integer.parseInt(matcher.group());
				if (gRoute.getRouteShortName().startsWith(T)) {
					return RID_STARTS_WITH_T + digits;
				}
			}
			throw new MTLog.Fatal("Unexpected route ID for %s!", gRoute);
		}
		return Long.parseLong(gRoute.getRouteShortName());
	}

	private static final String RSN_115_37 = "115-37";
	private static final String _115 = "115 ";
	private static final String RSN_37 = "37";
	private static final String T34 = "T34";
	private static final String T_34 = "T-34";

	@NotNull
	@Override
	public String getRouteLongName(@NotNull GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongNameOrDefault();
		routeLongName = CleanUtils.SAINT.matcher(routeLongName).replaceAll(CleanUtils.SAINT_REPLACEMENT);
		if (RSN_115_37.equals(gRoute.getRouteShortName())) {
			routeLongName = _115 + routeLongName;
		}
		return CleanUtils.cleanLabelFR(routeLongName);
	}

	@Nullable
	@Override
	public String getRouteShortName(@NotNull GRoute gRoute) {
		if (T_34.equals(gRoute.getRouteShortName())) {
			return T34;
		}
		if (RSN_115_37.equals(gRoute.getRouteShortName())) {
			return RSN_37;
		}
		return super.getRouteShortName(gRoute);
	}

	private static final String AGENCY_COLOR = "1F1F1F"; // DARK GRAY (from GTFS)

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final String AM = "AM";
	private static final String PM = "PM";

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		final String tripHeadsign = gTrip.getTripHeadsignOrDefault();
		if (mRoute.getId() == 200L) {
			if (tripHeadsign.equals("Métro Angrignon - Cégep André-Laurendeau")
					|| tripHeadsign.equals("Angrignon - André-Laurendeau")) {
				mTrip.setHeadsignString(
						cleanTripHeadsign(tripHeadsign) + " " + gTrip.getDirectionIdOrDefault(),
						gTrip.getDirectionIdOrDefault()
				);
				return;
			}
		}
		if (mRoute.getId() == 210L) {
			if (tripHeadsign.equals("Métro Longueuil - Cégep É-Montpetit")
					|| tripHeadsign.equals("Longueuil - Édouard-Montpetit")) {
				mTrip.setHeadsignString(
						cleanTripHeadsign(tripHeadsign) + " " + gTrip.getDirectionIdOrDefault(),
						gTrip.getDirectionIdOrDefault()
				);
				return;
			}
		}
		mTrip.setHeadsignString(
				cleanTripHeadsign(tripHeadsign),
				gTrip.getDirectionIdOrDefault()
		);
	}

	@NotNull
	@Override
	public String cleanDirectionHeadsign(boolean fromStopName, @NotNull String directionHeadSign) {
		final String directionHeadSignUC = directionHeadSign.toUpperCase(Locale.ENGLISH);
		if (directionHeadSignUC.endsWith(AM)) {
			return AM;
		} else if (directionHeadSignUC.endsWith(PM)) {
			return PM;
		}
		return super.cleanDirectionHeadsign(fromStopName, directionHeadSign);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@Override
	public boolean directionFinderEnabled(long routeId, @NotNull GRoute gRoute) {
		if (routeId == 200L
				|| routeId == 210L) {
			return false; // DISABLED because similar trip with different direction ID
		}
		return super.directionFinderEnabled(routeId, gRoute);
	}

	@Override
	public boolean mergeHeadsign(@NotNull MTrip mTrip, @NotNull MTrip mTripToMerge) {
		throw new MTLog.Fatal("Unexpected trips to merge %s & %s!", mTrip, mTripToMerge);
	}

	private static final Pattern DIRECTION = Pattern.compile("(direction )", Pattern.CASE_INSENSITIVE);
	private static final String DIRECTION_REPLACEMENT = "";

	private static final Pattern ANDRE_LAURENDEAU_ = CleanUtils.cleanWords(Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.CANON_EQ, "andré-laurendeau");
	private static final String ANDRE_LAURENDEAU_REPLACEMENT = CleanUtils.cleanWordsReplacement("A-Laurendeau");

	private static final Pattern EDOUARD_MONTPETIT_ = CleanUtils.cleanWords(Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.CANON_EQ, "édouard-montpetit");
	private static final String EDOUARD_MONTPETIT_REPLACEMENT = CleanUtils.cleanWordsReplacement("É-Montpetit");

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = DIRECTION.matcher(tripHeadsign).replaceAll(DIRECTION_REPLACEMENT);
		tripHeadsign = ANDRE_LAURENDEAU_.matcher(tripHeadsign).replaceAll(ANDRE_LAURENDEAU_REPLACEMENT);
		tripHeadsign = EDOUARD_MONTPETIT_.matcher(tripHeadsign).replaceAll(EDOUARD_MONTPETIT_REPLACEMENT);
		tripHeadsign = CleanUtils.removeVia(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypesFRCA(tripHeadsign);
		return CleanUtils.cleanLabelFR(tripHeadsign);
	}

	private static final Pattern START_WITH_FACE_A = Pattern.compile("^(face à )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.CANON_EQ);
	private static final Pattern START_WITH_FACE_AU = Pattern.compile("^(face au )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static final Pattern START_WITH_FACE = Pattern.compile("^(face )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	private static final Pattern SPACE_FACE_A = Pattern.compile("( face à )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.CANON_EQ);
	private static final Pattern SPACE_WITH_FACE_AU = Pattern.compile("( face au )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static final Pattern SPACE_WITH_FACE = Pattern.compile("( face )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	private static final Pattern[] START_WITH_FACES = new Pattern[]{START_WITH_FACE_A, START_WITH_FACE_AU, START_WITH_FACE};

	private static final Pattern[] SPACE_FACES = new Pattern[]{SPACE_FACE_A, SPACE_WITH_FACE_AU, SPACE_WITH_FACE};

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = Utils.replaceAll(gStopName, START_WITH_FACES, CleanUtils.SPACE);
		gStopName = Utils.replaceAll(gStopName, SPACE_FACES, CleanUtils.SPACE);
		gStopName = CleanUtils.cleanStreetTypesFRCA(gStopName);
		return CleanUtils.cleanLabelFR(gStopName);
	}

	@Override
	public int getStopId(@NotNull GStop gStop) {
		return Integer.parseInt(getStopCode(gStop)); // using stop code as stop ID
	}
}
