package mws.apps.greekquiz;



import android.provider.BaseColumns;

public interface Constants extends BaseColumns {

	//These strings will get passed into the db handler
	public static final String LEX_TABLE = "tLexicon";
		public static final String LEX_GRK = "greek";
		public static final String LEX_ENG = "english";
		public static final String LEX_SEC = "section";
		public static final String LEX_FLG = "flagsequence";
		public static final String LEX_SEQ = "sequence";
		public static final String LEX_ROOT = "root";
		public static final String LEX_EDT = "edit";
		
	public static final String SECTIONS_TABLE = "tSections";
		public static final String SEC_SECTION = "sectionTitle";
		public static final String SEC_FLG = "isSectionSelected";
		
	public static final String PREFS_NAME = "savedsettings";
	
		
		
}

