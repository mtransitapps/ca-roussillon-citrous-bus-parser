package org.mtransit.parser.ca_roussillon_citrous_bus;

import static org.mtransit.commons.Constants.SPACE_;
import static org.mtransit.commons.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.RegexUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.mt.data.MAgency;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

// https://exo.quebec/en/about/open-data
// https://exo.quebec/xdata/citrous/google_transit.zip
public class RoussillonCITROUSBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new RoussillonCITROUSBusAgencyTools().start(args);
	}

	@Nullable
	@Override
	public List<Locale> getSupportedLanguages() {
		return LANG_FR;
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "exo Roussillon";
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return true;
	}

	@Override
	public boolean defaultRouteLongNameEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String cleanRouteLongName(@NotNull String routeLongName) {
		routeLongName = CleanUtils.SAINT.matcher(routeLongName).replaceAll(CleanUtils.SAINT_REPLACEMENT);
		return CleanUtils.cleanLabelFR(routeLongName);
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@Override
	public boolean allowNonDescriptiveHeadSigns(long routeId) {
		if (routeId == 200L) {
			return true; // 2 very similar trips, same head-sign, same 1st/last stops
		}
		return super.allowNonDescriptiveHeadSigns(routeId);
	}

	private static final Pattern ANDRE_LAURENDEAU_ = CleanUtils.cleanWords(Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.CANON_EQ, "andré-laurendeau");
	private static final String ANDRE_LAURENDEAU_REPLACEMENT = CleanUtils.cleanWordsReplacement("A-Laurendeau");

	private static final Pattern EDOUARD_MONTPETIT_ = CleanUtils.cleanWords(Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.CANON_EQ, "édouard-montpetit");
	private static final String EDOUARD_MONTPETIT_REPLACEMENT = CleanUtils.cleanWordsReplacement("É-Montpetit");

	private static final Pattern _DASH_ = Pattern.compile("( - | – )");
	private static final String _DASH_REPLACEMENT = "<>"; // form<>to

	private static final Pattern ENDS_W_AM_PM_ = Pattern.compile("(^(.*) (am|pm)$)", Pattern.CASE_INSENSITIVE);
	// private static final String ENDS_W_AM_PM_KEEP_AM_PM = "$3";
	private static final String ENDS_W_AM_PM_KEEP_TEXT = "$2";

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = ENDS_W_AM_PM_.matcher(tripHeadsign).replaceAll(ENDS_W_AM_PM_KEEP_TEXT); // remove AM/PM
		tripHeadsign = ANDRE_LAURENDEAU_.matcher(tripHeadsign).replaceAll(ANDRE_LAURENDEAU_REPLACEMENT);
		tripHeadsign = EDOUARD_MONTPETIT_.matcher(tripHeadsign).replaceAll(EDOUARD_MONTPETIT_REPLACEMENT);
		tripHeadsign = _DASH_.matcher(tripHeadsign).replaceAll(_DASH_REPLACEMENT); // from - to => form<>to
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

	private static final Pattern DEVANT_ = CleanUtils.cleanWordsFR("devant");

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = _DASH_.matcher(gStopName).replaceAll(SPACE_);
		gStopName = DEVANT_.matcher(gStopName).replaceAll(EMPTY);
		gStopName = RegexUtils.replaceAllNN(gStopName, START_WITH_FACES, CleanUtils.SPACE);
		gStopName = RegexUtils.replaceAllNN(gStopName, SPACE_FACES, CleanUtils.SPACE);
		gStopName = CleanUtils.cleanStreetTypesFRCA(gStopName);
		return CleanUtils.cleanLabelFR(gStopName);
	}

	@Override
	public int getStopId(@NotNull GStop gStop) {
		return Integer.parseInt(getStopCode(gStop)); // using stop code as stop ID
	}
}
