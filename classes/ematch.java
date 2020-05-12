//
// Created:  02/10/2002
// Modified: 02/12/2002
//           04/05/2002
//           11/05/2003
//
//           02/19/2005, Oracle 10
//           04/06/2006, Uniform directory structure
//
//


import java.sql.*;
import java.math.*;
import java.io.*;
import java.util.*;
import java.awt.*;

import oracle.jdbc.driver.*;
import oracle.jdbc.*;

import javax.servlet.*;
import javax.servlet.http.*;

import oracle.jdbc.pool.OracleDataSource;


public class ematch extends HttpServlet {

    public ematchConstStruct constStruct = new ematchConstStruct();

    //private static String queryRows[];
    public static String resultRows[][];

    public static int numberOfRows;
    public static int numberOfMatchedRows;
    public static int numberOfColumns;

    public static int columnTypes[];
    public static String columnTypeNames[];
    public static int rowPreFetch;

    public static String paraValues[];
    public static int rating[];

    public static String askedLocationName;
    public static int askedLocationIndex;
    public static int askedMinSal = 0; // For job search

    public static int askedMaxSal = 0; // For member (candidate search)
    public static int askedDegree; // For member (candidate search)
    public static int askedYearOfExp; // For member (candidate search)

    public static int searchType; // 1: job search; 2: member search
    public static int locationSearchType = 1; // 1: by country; 2: by region; 3: by state; 4: by city
   
    int originalIndex[];

    int neighborCityBeginningIndex[];
    int neighborStateBeginningIndex[];
    int usCityInStateBeginningIndex[];
    int usStateInRegionBeginningIndex[];

    public ematch(int ematchOrInsert) {
        int i;

        if (ematchOrInsert == constStruct.EMATCH_) {

            neighborCityBeginningIndex = new int[constStruct.NUMBER_OF_US_CITIES];
            neighborStateBeginningIndex = new int[constStruct.NUMBER_OF_US_STATES];
            usCityInStateBeginningIndex = new int[constStruct.NUMBER_OF_US_STATES];
            usStateInRegionBeginningIndex = new int[constStruct.NUMBER_OF_US_REGIONS];

            neighborCityBeginningIndex[0] = 0;
            neighborStateBeginningIndex[0] = 0;
            usCityInStateBeginningIndex[0] = 0;
            usStateInRegionBeginningIndex[0] = 0;

            for (i = 1; i < constStruct.NUMBER_OF_US_CITIES; i++) {
                neighborCityBeginningIndex[i] =
                    neighborCityBeginningIndex[i - 1] +
                    2 * constStruct.usNeighborCityNumber[i - 1];
            }

            for (i = 1; i < constStruct.NUMBER_OF_US_STATES; i++) {
                usCityInStateBeginningIndex[i] += usCityInStateBeginningIndex[i - 1] +
                    constStruct.usCitiesInStatesNumber[i - 1];
            }

            for (i = 1; i < constStruct.NUMBER_OF_US_STATES; i++) {

                neighborStateBeginningIndex[i] =
                    neighborStateBeginningIndex[i - 1] +
                    2 * constStruct.usNeighborStateNumber[i - 1];
            }

            for (i = 1; i < constStruct.NUMBER_OF_US_REGIONS; i++) {
                usStateInRegionBeginningIndex[i] =
                    usStateInRegionBeginningIndex[i - 1] +
                    constStruct.usStatesInRegionsNumber[i - 1] + 1;
            }

            originalIndex = new int[constStruct.MAX_MATCH_NUMBER];
            for (i = 0; i < constStruct.MAX_MATCH_NUMBER; i++)
                originalIndex[i] = i;

            rating = new int[constStruct.MAX_MATCH_NUMBER];
            // Initial ratings
            for (i = 0; i < constStruct.MAX_MATCH_NUMBER; i++) {
                rating[i] = 100;
            }
        }

    }

    public String buildJobQuery(HttpServletRequest request, int sType)
    throws SQLException, IOException {

        String jobQuerySqlStmt;
        String minSal, locationType;

        int addAND = 0;


        searchType = sType;
        jobQuerySqlStmt = "SELECT * FROM job"; // base query

        paraValues = new String[constStruct.COLUMN_NUMBER_IN_JOB_TABLE];

        paraValues[constStruct.JOB_TABLE_JOB_TYPE] = String.valueOf(request.getParameter("jobType"));
		
        if (!paraValues[constStruct.JOB_TABLE_JOB_TYPE].equalsIgnoreCase("all")) {
            jobQuerySqlStmt += " where job_type='" + paraValues[constStruct.JOB_TABLE_JOB_TYPE] + "'";
            addAND = 1;
        }

		/********************* jobTitle **********************/	
		
        paraValues[constStruct.JOB_TABLE_JOB_TITLE] = request.getParameter("jobTitle");
		
		String a = paraValues[constStruct.JOB_TABLE_JOB_TITLE]; 
		
		String[] aSplit = a.split("\\s+");
		String lastOne = null; String lastSec = null;
		String[] aValid = new String[]{};
		
		if(aSplit.length >=3){
			 lastOne = aSplit[aSplit.length-1];
        	 lastSec = aSplit[aSplit.length-2];
			
			 aValid = new String[]{lastOne,lastSec};
		} else {
			 lastOne = aSplit[aSplit.length-1];

	         aValid = new String[]{lastOne};
		}
		
        if (!a.equalsIgnoreCase("all")) {
			for(String pin : aValid) {
				if (addAND == 1)
					jobQuerySqlStmt += " OR job_title LIKE '%" + pin + "%'";
				else
					jobQuerySqlStmt += " where job_title LIKE '%" + pin + "%'";
				addAND = 1;	
			}
        }
		 
		 
	    /********************* specialty **********************/	
		paraValues[constStruct.JOB_TABLE_SPECIALIZATION] = request.getParameter("specialty");
		
		a = paraValues[constStruct.JOB_TABLE_SPECIALIZATION]; 
		
		aSplit = a.split("\\s+");
		
		if(aSplit.length >=3){
			 lastOne = aSplit[aSplit.length-1];
        	 lastSec = aSplit[aSplit.length-2];
			
			 aValid = new String[]{lastOne,lastSec};
		} else {
			
		     aValid = aSplit;
		}
	
        if (!a.equalsIgnoreCase("all")) {
			
			for(String pin : aValid) {
		 
				if (addAND == 1)
					jobQuerySqlStmt += " OR specialization LIKE '%" + pin + "%'";
				else
					jobQuerySqlStmt += " where specialization LIKE '%" + pin + "%'";
				addAND = 1;
			
			}
        }
		 
		 
	    /********************* Description **********************/	
		paraValues[constStruct.JOB_TABLE_DESCRIPTION] = request.getParameter("field_keyword");
		 
		a = paraValues[constStruct.JOB_TABLE_DESCRIPTION]; 
		
		aSplit = a.split("\\s+");
 
	    aValid = aSplit;
		
		/**
		for(String pin : aValid) {
	 
			if (addAND == 1)
				jobQuerySqlStmt += " OR DESCRIPTION LIKE '%" + pin + "%'";
			
			else
				jobQuerySqlStmt += " where DESCRIPTION LIKE '%" + pin + "%'";
		 
			jobQuerySqlStmt += " OR QUALIFICATION LIKE '%" + pin + "%'";
			jobQuerySqlStmt += " OR CONTACT_PERSON LIKE '%" + pin + "%'";
			jobQuerySqlStmt += " OR LOCATION LIKE '%" + pin + "%'";
			jobQuerySqlStmt += " OR STATE_NAME LIKE '%" + pin + "%'";
			jobQuerySqlStmt += " OR REGION_NAME LIKE '%" + pin + "%'";	
			jobQuerySqlStmt += " OR job_title LIKE '%" + pin + "%'";	
	        jobQuerySqlStmt += " OR specialization LIKE '%" + pin + "%'";
			
			addAND = 1;   
		
		}
        */
	   
	   
		 
		 
		 
		/********************* Company Name **********************/	

        paraValues[constStruct.JOB_TABLE_COMPANY_NAME] = request.getParameter("companyName");

        if (!paraValues[constStruct.JOB_TABLE_COMPANY_NAME].equalsIgnoreCase("any")) {
            if (addAND == 1)
                jobQuerySqlStmt += " AND company_name='" + paraValues[constStruct.JOB_TABLE_COMPANY_NAME] + "'";
            else {
                jobQuerySqlStmt += " where company_name='" + paraValues[constStruct.JOB_TABLE_COMPANY_NAME] + "'";
                addAND = 1;
            }
        }

        minSal = request.getParameter("minSalary");
        if (!minSal.equals("Any")) {
            askedMinSal = Integer.parseInt(minSal);
            paraValues[constStruct.JOB_TABLE_MIN_SALARY] = minSal;
        }

        locationType = request.getParameter("locationType");
        if (locationType.equals("region")) {
            locationSearchType = constStruct.SEARCH_BY_REGION;
            askedLocationName = new String(request.getParameter("region"));
            askedLocationIndex = findRegionIndex(askedLocationName.toLowerCase());
            paraValues[constStruct.JOB_TABLE_REGION_NAME] = askedLocationName;
        } else if (locationType.equals("state")) {
            locationSearchType = constStruct.SEARCH_BY_STATE;
            askedLocationName = new String(request.getParameter("state"));
            askedLocationIndex = findStateIndex(askedLocationName.toLowerCase());
            paraValues[constStruct.JOB_TABLE_STATE_NAME] = askedLocationName;
        } else if (locationType.equals("city")) {
            locationSearchType = constStruct.SEARCH_BY_CITY;
            askedLocationName = new String(request.getParameter("city"));
            askedLocationIndex = findCityIndex(askedLocationName.toLowerCase());
            paraValues[constStruct.JOB_TABLE_LOCATION] = askedLocationName;
        }

        return jobQuerySqlStmt;
    }

    public String buildCandidateQuery(HttpServletRequest request, int sType)
    throws SQLException, IOException {
        String candidateQuerySqlStmt, job_type_value, job_title_value;
        String maxSal, locationType;

        int addAND = 0;


        searchType = sType;
        candidateQuerySqlStmt = "SELECT * FROM member"; // base query

        paraValues = new String[constStruct.COLUMN_NUMBER_IN_MEMBER_TABLE];

        paraValues[constStruct.MEMBER_TABLE_CURRENT_JOB_TITLE] = request.getParameter("jobTitle");

        if (!paraValues[constStruct.MEMBER_TABLE_CURRENT_JOB_TITLE].equalsIgnoreCase("all")) {
            if (addAND == 1)
                candidateQuerySqlStmt += " AND job_title='" +
                paraValues[constStruct.MEMBER_TABLE_CURRENT_JOB_TITLE] + "'";
            else
                candidateQuerySqlStmt += " where job_title='" +
                paraValues[constStruct.MEMBER_TABLE_CURRENT_JOB_TITLE] + "'";
            addAND = 1;
        }

        paraValues[constStruct.MEMBER_TABLE_SPECIALIZATION] = request.getParameter("specialty");

        if (!paraValues[constStruct.MEMBER_TABLE_SPECIALIZATION].equalsIgnoreCase("all")) {
            if (addAND == 1)
                candidateQuerySqlStmt += " AND specialization='" +
                paraValues[constStruct.MEMBER_TABLE_SPECIALIZATION] + "'";
            else {
                candidateQuerySqlStmt += " where specialization='" +
                    paraValues[constStruct.MEMBER_TABLE_SPECIALIZATION] + "'";
                addAND = 1;
            }
        }

        paraValues[constStruct.MEMBER_TABLE_DEGREE] = request.getParameter("degree");

        askedDegree = Integer.parseInt(paraValues[constStruct.MEMBER_TABLE_DEGREE]);

        paraValues[constStruct.MEMBER_TABLE_YEAR_OF_EXP] = request.getParameter("yearsOfExp");
        askedYearOfExp = Integer.parseInt(paraValues[constStruct.MEMBER_TABLE_YEAR_OF_EXP]);

        maxSal = request.getParameter("maxSalary");
        if (!maxSal.equals("Any")) {
            askedMaxSal = Integer.parseInt(maxSal);
            paraValues[constStruct.MEMBER_TABLE_DESIRED_SALARY] = maxSal;
        }

        locationType = request.getParameter("locationType");
        if (locationType.equals("region")) {
            locationSearchType = constStruct.SEARCH_BY_REGION;
            askedLocationName = new String(request.getParameter("region"));
            askedLocationIndex = findRegionIndex(askedLocationName.toLowerCase());
        } else if (locationType.equals("state")) {
            locationSearchType = constStruct.SEARCH_BY_STATE;
            askedLocationName = new String(request.getParameter("state"));
            askedLocationIndex = findStateIndex(askedLocationName.toLowerCase());
        } else if (locationType.equals("city")) {
            locationSearchType = constStruct.SEARCH_BY_CITY;
            askedLocationName = new String(request.getParameter("city"));
            askedLocationIndex = findCityIndex(askedLocationName.toLowerCase());
        }

        paraValues[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION] = askedLocationName;
        paraValues[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION_CODE] = Integer.toString(locationSearchType);

        return candidateQuerySqlStmt;
    }

    public String buildInsertJobStmt(HttpServletRequest request)
    throws SQLException, IOException {
        String insertJobStmt;
        int jobId;
        int numberOfMissingFields = 0;

        int jobTypeMissing = 0, jobTitleMissing, specializationMissing = 0;
        int regionMissing = 0, stateMissing = 0, cityMissing = 0;
        int companyNameMissing = 0, startDateMissing = 0;

        insertJobStmt = "INSERT INTO job VALUES ("; // base query

        jobId = getANewJobId();

        insertJobStmt += jobId + ", ";

        paraValues = new String[constStruct.COLUMN_NUMBER_IN_JOB_TABLE];

        paraValues[constStruct.JOB_TABLE_JOB_TYPE] = String.valueOf(request.getParameter("jobType"));
        if (paraValues[constStruct.JOB_TABLE_JOB_TYPE] != null) {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_JOB_TYPE] +
                paraValues[constStruct.JOB_TABLE_JOB_TYPE].trim() +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_JOB_TYPE] + ", ";
        } else {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_JOB_TYPE] +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_JOB_TYPE] + ", ";
            numberOfMissingFields++;
        }

        paraValues[constStruct.JOB_TABLE_JOB_TITLE] = request.getParameter("jobTitle");
        if (!paraValues[constStruct.JOB_TABLE_JOB_TITLE].equals("null")) {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_JOB_TITLE] +
                paraValues[constStruct.JOB_TABLE_JOB_TITLE].trim() +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_JOB_TITLE] + ", ";
        } else {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_JOB_TITLE] +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_JOB_TITLE] + ", ";
            numberOfMissingFields++;
        }

        paraValues[constStruct.JOB_TABLE_SPECIALIZATION] = request.getParameter("specialty");
        if (!paraValues[constStruct.JOB_TABLE_SPECIALIZATION].equals("null")) {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_SPECIALIZATION] +
                paraValues[constStruct.JOB_TABLE_SPECIALIZATION].trim() +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_SPECIALIZATION] + ", ";
        } else {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_SPECIALIZATION] +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_SPECIALIZATION] + ", ";
            numberOfMissingFields++;
        }

        paraValues[constStruct.JOB_TABLE_COUNTRY_CODE] = request.getParameter("countryCode");
        //if ( !paraValues[constStruct.JOB_TABLE_COUNTRY_CODE].equals("null") )
        //{
        //    insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_COUNTRY_CODE] +
        //   	paraValues[constStruct.JOB_TABLE_COUNTRY_CODE] +
        //	constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_COUNTRY_CODE] + ", ";
        //     } else { 
        insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_COMPANY_NAME] + "1" +
            constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_COMPANY_NAME] + ", ";
        //    }

        paraValues[constStruct.JOB_TABLE_REGION_NAME] = request.getParameter("regionName");
        if (paraValues[constStruct.JOB_TABLE_REGION_NAME].equals("Any") ||
            paraValues[constStruct.JOB_TABLE_REGION_NAME].equals("null")) {
            regionMissing = 1;
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_REGION_NAME] +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_REGION_NAME] + ", ";
        } else if (!paraValues[constStruct.JOB_TABLE_REGION_NAME].equals("null")) {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_REGION_NAME] +
                paraValues[constStruct.JOB_TABLE_REGION_NAME].trim() +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_REGION_NAME] + ", ";
        }

        paraValues[constStruct.JOB_TABLE_STATE_NAME] = request.getParameter("stateName");
        if (paraValues[constStruct.JOB_TABLE_STATE_NAME].equals("Any")) {
            stateMissing = 1;
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_STATE_NAME] +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_STATE_NAME] + ", ";
        } else if (!paraValues[constStruct.JOB_TABLE_STATE_NAME].equals("null")) {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_STATE_NAME] +
                paraValues[constStruct.JOB_TABLE_STATE_NAME].trim() +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_STATE_NAME] + ", ";
        }

        paraValues[constStruct.JOB_TABLE_LOCATION] = request.getParameter("location");
        if (paraValues[constStruct.JOB_TABLE_LOCATION].equals("Any")) {
            cityMissing = 1;
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_LOCATION] +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_LOCATION] + ", ";
        } else if (!paraValues[constStruct.JOB_TABLE_LOCATION].equals("null")) {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_LOCATION] +
                paraValues[constStruct.JOB_TABLE_LOCATION].trim() +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_LOCATION] + ", ";
        }

        paraValues[constStruct.JOB_TABLE_MIN_SALARY] = request.getParameter("minSalary");
        if (!paraValues[constStruct.JOB_TABLE_MIN_SALARY].equals("null")) {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_MIN_SALARY] +
                paraValues[constStruct.JOB_TABLE_MIN_SALARY].trim() +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_MIN_SALARY] + ", ";
        } else { // set min salary to 0 if it is not supplied
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_MIN_SALARY] + 0 +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_MIN_SALARY] + ", ";
        }

        paraValues[constStruct.JOB_TABLE_MAX_SALARY] = request.getParameter("maxSalary");
        if (!paraValues[constStruct.JOB_TABLE_MAX_SALARY].trim().equals("")) {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_MAX_SALARY] +
                paraValues[constStruct.JOB_TABLE_MAX_SALARY].trim() +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_MAX_SALARY] + ", ";
        } else {
            if (paraValues[constStruct.JOB_TABLE_MIN_SALARY].equals(""))
                insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_MAX_SALARY] + 0 +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_MAX_SALARY] + ", ";
            else // otherwise, set max salary same as min salary
                insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_MAX_SALARY] +
                paraValues[constStruct.JOB_TABLE_MIN_SALARY].trim() +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_MAX_SALARY] + ", ";
        }

        paraValues[constStruct.JOB_TABLE_COMPANY_NAME] = request.getParameter("companyName");
        if (paraValues[constStruct.JOB_TABLE_COMPANY_NAME].equals("null")) {
            numberOfMissingFields++;
            companyNameMissing = 1;
        } else {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_COMPANY_NAME] +
                paraValues[constStruct.JOB_TABLE_COMPANY_NAME].trim() +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_COMPANY_NAME] + ", ";
        }

        paraValues[constStruct.JOB_TABLE_START_DATE] = request.getParameter("startDate");
        if (paraValues[constStruct.JOB_TABLE_START_DATE].equals("null")) {
            startDateMissing = 1;
            numberOfMissingFields++;
        } else {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_START_DATE] +
                paraValues[constStruct.JOB_TABLE_START_DATE].trim() +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_START_DATE] + ", ";
        }

        paraValues[constStruct.JOB_TABLE_REFERENCE_NUM] = request.getParameter("referenceNum");
        if (!paraValues[constStruct.JOB_TABLE_REFERENCE_NUM].equals("null")) {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_REFERENCE_NUM] +
                paraValues[constStruct.JOB_TABLE_REFERENCE_NUM].trim() +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_REFERENCE_NUM] + ", ";
        } else {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_REFERENCE_NUM] +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_REFERENCE_NUM] + ", ";
        }

        paraValues[constStruct.JOB_TABLE_CONTACT_PERSON] = request.getParameter("contactPerson");
        if (!paraValues[constStruct.JOB_TABLE_CONTACT_PERSON].equals("null")) {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_CONTACT_PERSON] +
                paraValues[constStruct.JOB_TABLE_CONTACT_PERSON].trim() +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_CONTACT_PERSON] + ", ";
        } else {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_CONTACT_PERSON] +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_CONTACT_PERSON] + ", ";
        }

        paraValues[constStruct.JOB_TABLE_DESCRIPTION] = request.getParameter("description");
        if (paraValues[constStruct.JOB_TABLE_DESCRIPTION] != null) {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_DESCRIPTION] +
                paraValues[constStruct.JOB_TABLE_DESCRIPTION].trim() +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_DESCRIPTION] + ", ";
        } else {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_DESCRIPTION] +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_DESCRIPTION] + ", ";
        }

        paraValues[constStruct.JOB_TABLE_QUALIFICATION] = request.getParameter("qualification");
        if (paraValues[constStruct.JOB_TABLE_QUALIFICATION] != null) {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_QUALIFICATION] +
                paraValues[constStruct.JOB_TABLE_QUALIFICATION].trim() +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_QUALIFICATION] + ")";
        } else {
            insertJobStmt += constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_QUALIFICATION] +
                constStruct.jobTableColumnQuotes[constStruct.JOB_TABLE_QUALIFICATION] + ")";
        }

        if ((regionMissing == 1) && (stateMissing == 1) && (cityMissing == 1))
            return "Error: at least one of region, state, city names should be given: " +
                insertJobStmt;
        else if (numberOfMissingFields > 0)
            return "Error: total " + numberOfMissingFields + " number of required fields missing:" +
                insertJobStmt;

        return insertJobStmt;
    } // End of buildInsertJobStmt()

    public String buildInsertEmployerStmt(HttpServletRequest request)
    throws SQLException, IOException {
        String insertEmpStmt;
        String empId;
        int numberOfMissingFields = 0;

        int empIdMissing = 0, companyNameMissing = 0, contactPersonMissing = 0;
        int emailMissing, passwordMissing = 0;

        insertEmpStmt = "INSERT INTO employer VALUES ("; // base query


        paraValues = new String[constStruct.COLUMN_NUMBER_IN_EMPLOYER_TABLE];

        paraValues[constStruct.EMPLOYER_TABLE_EMP_ID] = String.valueOf(request.getParameter("empId")).trim();
        if ((paraValues[constStruct.EMPLOYER_TABLE_EMP_ID] == null) ||
            (paraValues[constStruct.EMPLOYER_TABLE_EMP_ID].equals("")))
            return "Error: Missing employer Id";
        else if (!idIsUnique(paraValues[constStruct.EMPLOYER_TABLE_EMP_ID],
                constStruct.EMPLOYER_TABLE_INDEX))
            return "Error: selected employer Id is already being used. Please choose a different one";
        insertEmpStmt +=
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_EMP_ID] +
            paraValues[constStruct.EMPLOYER_TABLE_EMP_ID] +
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_EMP_ID] + ", ";

        paraValues[constStruct.EMPLOYER_TABLE_COMPANY_NAME] = String.valueOf(request.getParameter("companyName")).trim();
        if ((paraValues[constStruct.EMPLOYER_TABLE_COMPANY_NAME] == null) ||
            (paraValues[constStruct.EMPLOYER_TABLE_COMPANY_NAME].equals("")))
            return "Error: Missing company name";
        insertEmpStmt +=
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_COMPANY_NAME] +
            paraValues[constStruct.EMPLOYER_TABLE_COMPANY_NAME] +
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_COMPANY_NAME] + ", ";

        paraValues[constStruct.EMPLOYER_TABLE_CONTACT_PERSON] = String.valueOf(request.getParameter("contactPerson")).trim();
        if ((paraValues[constStruct.EMPLOYER_TABLE_CONTACT_PERSON] == null) ||
            (paraValues[constStruct.EMPLOYER_TABLE_CONTACT_PERSON].equals("")))
            return "Error: Missing contact person name";
        insertEmpStmt +=
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_CONTACT_PERSON] +
            paraValues[constStruct.EMPLOYER_TABLE_CONTACT_PERSON] +
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_CONTACT_PERSON] + ", ";

        paraValues[constStruct.EMPLOYER_TABLE_EMAIL] = String.valueOf(request.getParameter("email")).trim();
        if ((paraValues[constStruct.EMPLOYER_TABLE_EMAIL] == null) ||
            (paraValues[constStruct.EMPLOYER_TABLE_EMAIL].equals("")))
            return "Error: Missing contact person name";
        insertEmpStmt +=
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_EMAIL] +
            paraValues[constStruct.EMPLOYER_TABLE_EMAIL] +
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_EMAIL] + ", ";

        paraValues[constStruct.EMPLOYER_TABLE_PHONE] = String.valueOf(request.getParameter("phone")).trim();
        if (paraValues[constStruct.EMPLOYER_TABLE_PHONE] == null)
            insertEmpStmt +=
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_PHONE] +
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_PHONE] + ", ";
        else
            insertEmpStmt +=
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_PHONE] +
            paraValues[constStruct.EMPLOYER_TABLE_PHONE] +
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_PHONE] + ", ";

        paraValues[constStruct.EMPLOYER_TABLE_FAX] = String.valueOf(request.getParameter("fax")).trim();
        if (paraValues[constStruct.EMPLOYER_TABLE_FAX] == null)
            insertEmpStmt +=
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_FAX] +
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_FAX] + ", ";
        else
            insertEmpStmt +=
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_FAX] +
            paraValues[constStruct.EMPLOYER_TABLE_FAX] +
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_FAX] + ", ";

        paraValues[constStruct.EMPLOYER_TABLE_PROFILE] = String.valueOf(request.getParameter("profile")).trim();
        if (paraValues[constStruct.EMPLOYER_TABLE_PROFILE] == null)
            insertEmpStmt +=
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_PROFILE] +
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_PROFILE] + ", ";
        else
            insertEmpStmt +=
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_PROFILE] +
            paraValues[constStruct.EMPLOYER_TABLE_PROFILE] +
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_PROFILE] + ", ";

        paraValues[constStruct.EMPLOYER_TABLE_PASSWORD] = String.valueOf(request.getParameter("password")).trim();
        if ((paraValues[constStruct.EMPLOYER_TABLE_PASSWORD] == null) ||
            (paraValues[constStruct.EMPLOYER_TABLE_PASSWORD].equals("")))
            return "Error: Missing password";
        else if (paraValues[constStruct.EMPLOYER_TABLE_PASSWORD].length() <= 5)
            return "Error: Password should be less than six characters";
        insertEmpStmt +=
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_PASSWORD] +
            paraValues[constStruct.EMPLOYER_TABLE_PASSWORD] +
            constStruct.employerTableColumnQuotes[constStruct.EMPLOYER_TABLE_PASSWORD] + ") ";

        return insertEmpStmt;
    } // End of buildInsertEmployerStmt()

    public String buildInsertCandidateStmt(HttpServletRequest request)
    throws SQLException, IOException {
        String insertCandidateStmt;
        String loginId, locationType;
        int numberOfMissingFields = 0;

        int empIdMissing = 0, companyNameMissing = 0, contactPersonMissing = 0;
        int emailMissing, passwordMissing = 0;

        insertCandidateStmt = "INSERT INTO member VALUES ("; // base query


        paraValues = new String[constStruct.COLUMN_NUMBER_IN_MEMBER_TABLE];

        paraValues[constStruct.MEMBER_TABLE_LOGIN_ID] = String.valueOf(request.getParameter("loginId")).trim();
        if ((paraValues[constStruct.MEMBER_TABLE_LOGIN_ID] == null) ||
            (paraValues[constStruct.MEMBER_TABLE_LOGIN_ID].equals("")))
            return "Error: Missing login Id";
        else if (!idIsUnique(paraValues[constStruct.MEMBER_TABLE_LOGIN_ID],
                constStruct.MEMBER_TABLE_INDEX))
            return "Error: selected login Id is already being used. Please choose a different one";
        insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_LOGIN_ID] +
            paraValues[constStruct.MEMBER_TABLE_LOGIN_ID] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_LOGIN_ID] + ", ";

        paraValues[constStruct.MEMBER_TABLE_FIRST_NAME] = String.valueOf(request.getParameter("firstName")).trim();
        if ((paraValues[constStruct.MEMBER_TABLE_FIRST_NAME] == null) ||
            (paraValues[constStruct.MEMBER_TABLE_FIRST_NAME].equals("")))
            return "Error: Missing first name";
        insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_FIRST_NAME] +
            paraValues[constStruct.MEMBER_TABLE_FIRST_NAME] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_FIRST_NAME] + ", ";

        paraValues[constStruct.MEMBER_TABLE_MID_I_NAME] = String.valueOf(request.getParameter("midIName")).trim();
        if ((paraValues[constStruct.MEMBER_TABLE_MID_I_NAME].equals("null")) ||
            (paraValues[constStruct.MEMBER_TABLE_MID_I_NAME].equals("")))
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_MID_I_NAME] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_MID_I_NAME] + ", ";
        else
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_MID_I_NAME] +
            paraValues[constStruct.MEMBER_TABLE_MID_I_NAME] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_MID_I_NAME] + ", ";

        paraValues[constStruct.MEMBER_TABLE_LAST_NAME] = String.valueOf(request.getParameter("lastName")).trim();
        if ((paraValues[constStruct.MEMBER_TABLE_LAST_NAME].equals("null")) ||
            (paraValues[constStruct.MEMBER_TABLE_LAST_NAME].equals("")))
            return "Error: Missing last name";
        insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_LAST_NAME] +
            paraValues[constStruct.MEMBER_TABLE_LAST_NAME] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_LAST_NAME] + ", ";

        paraValues[constStruct.MEMBER_TABLE_SPECIALIZATION] = request.getParameter("specialty");
        if (paraValues[constStruct.MEMBER_TABLE_SPECIALIZATION].equals("null")) {
            return "Error: Missing specialty";
        } else {
            insertCandidateStmt += constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_SPECIALIZATION] +
                paraValues[constStruct.MEMBER_TABLE_SPECIALIZATION] +
                constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_SPECIALIZATION] + ", ";
        }

        paraValues[constStruct.MEMBER_TABLE_EMAIL] = String.valueOf(request.getParameter("email")).trim();
        if ((paraValues[constStruct.MEMBER_TABLE_EMAIL].equals("null")) ||
            (paraValues[constStruct.MEMBER_TABLE_EMAIL].equals("")))
            return "Error: Missing email";
        insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_EMAIL] +
            paraValues[constStruct.MEMBER_TABLE_EMAIL] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_EMAIL] + ", ";

        paraValues[constStruct.MEMBER_TABLE_PHONE] = String.valueOf(request.getParameter("phone")).trim();
        if (paraValues[constStruct.MEMBER_TABLE_PHONE].equals("null"))
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_PHONE] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_PHONE] + ", ";
        else
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_PHONE] +
            paraValues[constStruct.MEMBER_TABLE_PHONE] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_PHONE] + ", ";

        paraValues[constStruct.MEMBER_TABLE_FAX] = String.valueOf(request.getParameter("fax")).trim();
        if (paraValues[constStruct.MEMBER_TABLE_FAX].equals("null"))
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_FAX] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_FAX] + ", ";
        else
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_FAX] +
            paraValues[constStruct.MEMBER_TABLE_FAX] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_FAX] + ", ";

        paraValues[constStruct.MEMBER_TABLE_WEB_URL] = String.valueOf(request.getParameter("webURL")).trim();
        if (paraValues[constStruct.MEMBER_TABLE_WEB_URL].equals("null"))
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_WEB_URL] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_WEB_URL] + ", ";
        else
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_WEB_URL] +
            paraValues[constStruct.MEMBER_TABLE_WEB_URL] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_WEB_URL] + ", ";

        paraValues[constStruct.MEMBER_TABLE_CURRENT_COMPANY] =
            String.valueOf(request.getParameter("currentCompany")).trim();
        if (paraValues[constStruct.MEMBER_TABLE_CURRENT_COMPANY].equals("null"))
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_CURRENT_COMPANY] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_CURRENT_COMPANY] + ", ";
        else
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_CURRENT_COMPANY] +
            paraValues[constStruct.MEMBER_TABLE_CURRENT_COMPANY] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_CURRENT_COMPANY] + ", ";

        paraValues[constStruct.MEMBER_TABLE_CURRENT_JOB_TITLE] =
            String.valueOf(request.getParameter("currentJobTitle")).trim();
        if ((paraValues[constStruct.MEMBER_TABLE_CURRENT_JOB_TITLE].equals("null")) ||
            (paraValues[constStruct.MEMBER_TABLE_CURRENT_JOB_TITLE].equals("")))
            return "Error: Missing current job title";
        insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_CURRENT_JOB_TITLE] +
            paraValues[constStruct.MEMBER_TABLE_CURRENT_JOB_TITLE] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_CURRENT_JOB_TITLE] + ", ";

        locationType = request.getParameter("currentJobLocationType");
        if (locationType.equals("region")) {
            paraValues[constStruct.MEMBER_TABLE_CURRENT_JOB_LOCATION] = request.getParameter("currentRegion");
            paraValues[constStruct.MEMBER_TABLE_CURRENT_JOB_LOCATION_CODE] =
                Integer.toString(constStruct.SEARCH_BY_REGION);
        } else if (locationType.equals("state")) {
            paraValues[constStruct.MEMBER_TABLE_CURRENT_JOB_LOCATION] = request.getParameter("currentState");
            paraValues[constStruct.MEMBER_TABLE_CURRENT_JOB_LOCATION_CODE] =
                Integer.toString(constStruct.SEARCH_BY_STATE);
        } else if (locationType.equals("city")) {
            paraValues[constStruct.MEMBER_TABLE_CURRENT_JOB_LOCATION] = request.getParameter("currentCity");
            paraValues[constStruct.MEMBER_TABLE_CURRENT_JOB_LOCATION_CODE] =
                Integer.toString(constStruct.SEARCH_BY_CITY);
        }

        insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_CURRENT_JOB_LOCATION] +
            paraValues[constStruct.MEMBER_TABLE_CURRENT_JOB_LOCATION] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_CURRENT_JOB_LOCATION] + ", ";

        insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_CURRENT_JOB_LOCATION_CODE] +
            paraValues[constStruct.MEMBER_TABLE_CURRENT_JOB_LOCATION_CODE] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_CURRENT_JOB_LOCATION_CODE] + ", ";

        paraValues[constStruct.MEMBER_TABLE_YEAR_OF_EXP] =
            String.valueOf(request.getParameter("yearOfExp")).trim();
        if ((paraValues[constStruct.MEMBER_TABLE_YEAR_OF_EXP].equals("null")) ||
            (paraValues[constStruct.MEMBER_TABLE_YEAR_OF_EXP].equals("")))
            return "Error: Missing years of experience";
        insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_YEAR_OF_EXP] +
            paraValues[constStruct.MEMBER_TABLE_YEAR_OF_EXP] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_YEAR_OF_EXP] + ", ";

        paraValues[constStruct.MEMBER_TABLE_DEGREE] =
            String.valueOf(request.getParameter("degree")).trim();
        if ((paraValues[constStruct.MEMBER_TABLE_DEGREE].equals("null")) ||
            (paraValues[constStruct.MEMBER_TABLE_DEGREE].equals("")))
            return "Error: Missing degree";
        insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DEGREE] +
            paraValues[constStruct.MEMBER_TABLE_DEGREE] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DEGREE] + ", ";

        paraValues[constStruct.MEMBER_TABLE_DESIRED_JOB_1] =
            String.valueOf(request.getParameter("desiredJob1")).trim();
        if (paraValues[constStruct.MEMBER_TABLE_DESIRED_JOB_1].equals("null"))
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DESIRED_JOB_1] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DESIRED_JOB_1] + ", ";
        else
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DESIRED_JOB_1] +
            paraValues[constStruct.MEMBER_TABLE_DESIRED_JOB_1] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DESIRED_JOB_1] + ", ";

        paraValues[constStruct.MEMBER_TABLE_DESIRED_JOB_2] =
            String.valueOf(request.getParameter("desiredJob2")).trim();
        if (paraValues[constStruct.MEMBER_TABLE_DESIRED_JOB_2].equals("null"))
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DESIRED_JOB_2] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DESIRED_JOB_2] + ", ";
        else
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DESIRED_JOB_2] +
            paraValues[constStruct.MEMBER_TABLE_DESIRED_JOB_2] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DESIRED_JOB_2] + ", ";

        paraValues[constStruct.MEMBER_TABLE_DESIRED_JOB_3] =
            String.valueOf(request.getParameter("desiredJob3")).trim();
        if (paraValues[constStruct.MEMBER_TABLE_DESIRED_JOB_3].equals("null"))
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DESIRED_JOB_3] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DESIRED_JOB_3] + ", ";
        else
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DESIRED_JOB_3] +
            paraValues[constStruct.MEMBER_TABLE_DESIRED_JOB_3] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DESIRED_JOB_3] + ", ";

        paraValues[constStruct.MEMBER_TABLE_DESIRED_SALARY] =
            String.valueOf(request.getParameter("desiredSalary")).trim();
        if (paraValues[constStruct.MEMBER_TABLE_DESIRED_SALARY] == null)
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DESIRED_SALARY] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DESIRED_SALARY] + ", ";
        else
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DESIRED_SALARY] +
            paraValues[constStruct.MEMBER_TABLE_DESIRED_SALARY] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DESIRED_SALARY] + ", ";

        locationType = request.getParameter("desiredJobLocationType");
        if (locationType.equals("region")) {
            paraValues[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION] = request.getParameter("desiredRegion");
            paraValues[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION_CODE] =
                Integer.toString(constStruct.SEARCH_BY_REGION);
        } else if (locationType.equals("state")) {
            paraValues[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION] = request.getParameter("desiredState");
            paraValues[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION_CODE] =
                Integer.toString(constStruct.SEARCH_BY_STATE);
        } else if (locationType.equals("city")) {
            paraValues[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION] = request.getParameter("desiredCity");
            paraValues[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION_CODE] =
                Integer.toString(constStruct.SEARCH_BY_CITY);
        }

        //paraValues[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION] =
        //		String.valueOf(request.getParameter("desiredJobLocation")).trim();
        insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION] +
            paraValues[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION] + ", ";

        insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION_CODE] +
            paraValues[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION_CODE] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION_CODE] + ", ";

        paraValues[constStruct.MEMBER_TABLE_SPECIAL_TALENTS] =
            String.valueOf(request.getParameter("specialTalents")).trim();
        if (paraValues[constStruct.MEMBER_TABLE_SPECIAL_TALENTS].equals("null"))
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_SPECIAL_TALENTS] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_SPECIAL_TALENTS] + ", ";
        else
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_SPECIAL_TALENTS] +
            paraValues[constStruct.MEMBER_TABLE_SPECIAL_TALENTS] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_SPECIAL_TALENTS] + ", ";

        paraValues[constStruct.MEMBER_TABLE_RESUME] =
            String.valueOf(request.getParameter("resume")).trim();
        if (paraValues[constStruct.MEMBER_TABLE_RESUME] == null)
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_RESUME] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_RESUME] + ")";
        else
            insertCandidateStmt +=
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_RESUME] +
            paraValues[constStruct.MEMBER_TABLE_RESUME] +
            constStruct.memberTableColumnQuotes[constStruct.MEMBER_TABLE_RESUME] + ")";

        return insertCandidateStmt;
    } // End of buildInsertCandidateStmt()


    public void getAndDisplayAJobDetails(HttpServletRequest request,
        HttpServletResponse response, PrintWriter out)
    throws SQLException, IOException {
        String queryStmt;
        String jobId;

        String oneColumn;

        jobId = String.valueOf(request.getParameter("job_id")).trim();
        queryStmt = "SELECT * FROM job WHERE job_id=" + jobId; // query

        // Load Oracle driver
        //DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
        //DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());

        OracleDataSource ods = new OracleDataSource();
        ods.setURL("jdbc:oracle:oci8:" + constStruct.USERNAME + "/" + constStruct.PASSWORD + "@" + constStruct.DATABASE);
        Connection conn = ods.getConnection();

        //Connection conn = DriverManager.getConnection
        //     ("jdbc:oracle:oci:@gla92010", constStruct.USERNAME, constStruct.PASSWORD);

        // Query the employee names 
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery(queryStmt);

        // Found the number of columns
        rset.next();

        out.println("<h3>Job Description</h3>");
        out.println("<UL>");
        out.println("<LI> Job Type:          ");
        oneColumn = new String(rset.getString(2));
        out.println("<font color=\"#900000\">");
        out.println(oneColumn);
        out.println("</font>");
        out.println("<br>");

        out.println("<LI> Job Title:           ");
        oneColumn = new String(rset.getString(3));
        out.println("<font color=\"#900000\">");
        out.println(oneColumn);
        out.println("</font>");
        out.println("<br>");

        out.println("<LI>Specialization:       ");
        oneColumn = rset.getString(4);
        out.println("<font color=\"#900000\">");
        out.println(oneColumn);
        out.println("</font>");
        out.println("<br>");

        out.println("<LI>Location:                ");
        oneColumn = rset.getString(8);
        out.println("<font color=\"#900000\">");
        if (oneColumn != null) {
            out.print(oneColumn); // city
            oneColumn = rset.getString(7);
            if (oneColumn != null) {
                out.print(" (" + oneColumn); // state
                oneColumn = rset.getString(7);
                if (oneColumn != null)
                    out.print(", " + oneColumn); // region
                out.print(")");
            } else {
                oneColumn = rset.getString(6);
                if (oneColumn != null)
                    out.print(" (" + oneColumn + ")"); // state
            }
            out.println("");
        } else {
            oneColumn = rset.getString(7);
            if (oneColumn != null) {
                out.print(oneColumn); // state
                oneColumn = rset.getString(6);
                if (oneColumn != null)
                    out.print(" (" + oneColumn + ")"); // state
            } else
                out.println(rset.getString(6)); // region
        }
        out.println("</font>");
        out.println("<br>");


        out.println("<LI>Salary:                ");
        oneColumn = rset.getString(9);
        out.println("<font color=\"#900000\">");
        if (oneColumn == null)
            out.println("N/A--");
        else
            out.println(oneColumn + "--");
        oneColumn = rset.getString(10);
        if (oneColumn == null)
            out.println("N/A");
        else
            out.println(oneColumn);
        out.println("</font>");
        out.println("<br>");

        out.println("<LI>Job Description:                  ");
        oneColumn = rset.getString(15);
        out.println("<font color=\"#900000\">");
        if (oneColumn == null)
            out.println("N/A");
        else
            out.println(oneColumn);
        out.println("</font>");
        out.println("<br>");

        out.println("</UL>");

        //close the result set, statement, and the connection
        rset.close();
        stmt.close();


    } // End of getAndDisplayAJobDetails()


    public void getAndDisplayACandidate(HttpServletRequest request,
        HttpServletResponse response, PrintWriter out)
    throws SQLException, IOException {
        String queryStmt;
        String loginId;

        String oneColumn;

        loginId = String.valueOf(request.getParameter("login_id")).trim();
        queryStmt = "SELECT * FROM member WHERE login_id='" + loginId + "'"; // query

        // Load Oracle driver
        //DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
        //DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());

        //Connection conn = DriverManager.getConnection
        //     ("jdbc:oracle:oci:@gla92010", constStruct.USERNAME, constStruct.PASSWORD);

        OracleDataSource ods = new OracleDataSource();
        ods.setURL("jdbc:oracle:oci8:" + constStruct.USERNAME + "/" + constStruct.PASSWORD + "@" + constStruct.DATABASE);
        Connection conn = ods.getConnection();
        // Query the employee names 
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery(queryStmt);

        // Found the number of columns
        ResultSetMetaData rsmd = rset.getMetaData();
        numberOfColumns = rsmd.getColumnCount();

        rset.next();

        //out.println("<center>");
        //out.println("<h3>Details Information for candidate </h3>");
        //out.println("<body background=\"/peng/images/sky_bg.gif\"" +
        //			"text=\"#000000\" link=\"8FF0FF0\" vlink=\"#551A8B\" alink=\"#FF0000\">");
        out.println("<h3>Detail Information for Candidate " + rset.getString(4) + " </h3>");
        //out.println("<table >");
        //out.println("<th>First Name");
        out.println("<UL>");
        out.println("<LI> First Name:          ");
        oneColumn = new String(rset.getString(2));
        out.println("<font color=\"#900000\">");
        out.println(oneColumn);
        out.println("<br>");
        out.println("</font>");

        out.println("<LI> Last Name:           ");
        oneColumn = new String(rset.getString(4));
        out.println("<font color=\"#900000\">");
        out.println(oneColumn);
        out.println("</font>");
        out.println("<br>");

        out.println("<LI>Specialization:       ");
        out.println("<font color=\"#900000\">");
        oneColumn = rset.getString(5);
        out.println(oneColumn);
        out.println("</font>");
        out.println("<br>");

        out.println("<LI>Email:                ");
        oneColumn = rset.getString(6);
        out.println("<font color=\"#900000\">");
        out.println("<A HREF=\"mailto:" + oneColumn + "\">" + oneColumn +
            "</A> <IMG SRC=/peng/images/email.gif border=0 align=middle>");
        out.println("</font>");
        out.println("<br>");


        out.println("<LI>Phone:                ");
        oneColumn = rset.getString(7);
        out.println("<font color=\"#900000\">");
        if (oneColumn == null)
            out.println("N/A");
        else
            out.println(oneColumn);
        out.println("</font>");
        out.println("<br>");

        out.println("<LI>Fax:                  ");
        oneColumn = rset.getString(8);
        out.println("<font color=\"#900000\">");
        if (oneColumn == null)
            out.println("N/A");
        else
            out.println(oneColumn);
        out.println("</font>");
        out.println("<br>");

        out.println("<LI>Web_URL:              ");
        oneColumn = rset.getString(9);
        out.println("<font color=\"#900000\">");
        if (oneColumn == null)
            out.println("N/A");
        else
            out.println(oneColumn);
        out.println("</font>");
        out.println("<br>");

        out.println("<LI>Current Company:      ");
        oneColumn = rset.getString(10);
        out.println("<font color=\"#900000\">");
        if (oneColumn == null)
            out.println("N/A");
        else
            out.println(oneColumn);
        out.println("</font>");
        out.println("<br>");

        out.println("<LI>Job Title:            ");
        oneColumn = rset.getString(11);
        out.println("<font color=\"#900000\">");
        if (oneColumn == null)
            out.println("N/A");
        else
            out.println(oneColumn);
        out.println("</font>");
        out.println("<br>");

        out.println("<LI>Years of exp:         ");
        oneColumn = rset.getString(14);
        out.println("<font color=\"#900000\">");
        if (oneColumn == null)
            out.println("N/A");
        else
            out.println(oneColumn);
        out.println("</font>");
        out.println("<br>");

        out.println("<LI>Degree:               ");
        oneColumn = rset.getString(15);
        out.println("<font color=\"#900000\">");
        if (oneColumn == null)
            out.println("N/A");
        else {
            out.println(constStruct.degreeNames[Integer.parseInt(oneColumn)]);
        }
        out.println("</font>");
        out.println("<br>");

        out.println("<LI>Desired Salary:       ");
        oneColumn = rset.getString(19);
        out.println("<font color=\"#900000\">");
        if (oneColumn == null)
            out.println("N/A");
        else
            out.println(oneColumn);
        out.println("</font>");
        out.println("<br>");

        out.println("<LI>Desired Job Location: ");
        oneColumn = rset.getString(20);
        out.println("<font color=\"#900000\">");
        if (oneColumn == null)
            out.println("N/A");
        else
            out.println(oneColumn);
        out.println("</font>");
        out.println("<br>");

        out.println("</UL>");

        //close the result set, statement, and the connection
        rset.close();
        stmt.close();

    } // End of getAndDisplayACandidate()


    public void doJobQuery(String sqlStmt, PrintWriter out)
    throws SQLException, IOException, ServletException {
        // Load Oracle driver
        // DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
        //DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());

        //Class.forName ("oracle.jdbc.driver.OracleDriver");
        // Connect to the local database
        //Connection conn = new oracle.jdbc.driver.OracleDriver().defaultConnection(); 
        //Connection conn = DriverManager.getConnection 
        //	("jdbc:oracle:kprb:@ORCL817","scott", "tiger");
        //          ("jdbc:oracle:kprb:@glaciers.cs.txstate.edu:1521:ORCL817","scott", "tiger"); 
        //Connection conn =  
        //     new oracle.jdbc.driver.OracleDriver().defaultConnection();

        //Connection conn = DriverManager.getConnection
        //     ("jdbc:oracle:oci:@gla92010", constStruct.USERNAME, constStruct.PASSWORD);

        OracleDataSource ods = new OracleDataSource();
        ods.setURL("jdbc:oracle:oci8:" + constStruct.USERNAME + "/" + constStruct.PASSWORD + "@" + constStruct.DATABASE);
        Connection conn = ods.getConnection();

        // Query the employee names 
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery(sqlStmt);

        // Found the number of columns
        ResultSetMetaData rsmd = rset.getMetaData();
        numberOfColumns = rsmd.getColumnCount();

        rowPreFetch = ((OracleStatement) stmt).getRowPrefetch();
        columnTypes = new int[numberOfColumns + 1];
        columnTypeNames = new String[numberOfColumns + 1];

        //String c = new String(rsmd.getColumnType(1));
        for (int i = 1; i <= numberOfColumns; i++) {

            //c += " " + new String(rsmd.getColumnType(i));
            columnTypes[i] = rsmd.getColumnType(i);
            columnTypeNames[i] = rsmd.getColumnTypeName(i);
        }

        //queryRows = new String [constStruct.MAX_ROW_NUMBER];
        resultRows = new String[constStruct.MAX_MATCH_NUMBER][];

        int tmpRating;
        int j = 0, k = 0;
        String currentRow[];
        while (rset.next()) {
 
            currentRow = new String[numberOfColumns + 1];
            currentRow[1] = new String(rset.getString(1));

            // Retrieve all columns of current row
            for (int i = 2; i <= numberOfColumns; i++) {
                currentRow[i] = rset.getString(i);
                //currentRow[i] = new String(rset.getString(1));
            }
 
            tmpRating = computeAJobRating(currentRow, out);
 
            if (tmpRating > 0) {
                resultRows[j] = currentRow;
                rating[j++] = tmpRating;
            }
        }
        //close the result set, statement, and the connection
        rset.close();
        stmt.close();
        conn.close();

        numberOfMatchedRows = j;

        sortResultRows(out);

    } // End of doJobQuery 

    public void doCandidateQuery(String sqlStmt, PrintWriter out)
    throws SQLException, IOException, ServletException {
        // Load Oracle driver
        // DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());

        //Connection conn = new oracle.jdbc.driver.OracleDriver().defaultConnection(); 
        //Connection conn = DriverManager.getConnection 
        //	("jdbc:oracle:kprb:@ORCL817","scott", "tiger");
        //          ("jdbc:oracle:kprb:@glaciers.cs.txstate.edu:1521:ORCL817","scott", "tiger"); 
        //Connection conn =  
        //     new oracle.jdbc.driver.OracleDriver().defaultConnection();

        // Connect to the local database
        //Connection conn = DriverManager.getConnection
        //     ("jdbc:oracle:oci:@gla92010", constStruct.USERNAME, constStruct.PASSWORD);

        OracleDataSource ods = new OracleDataSource();
        ods.setURL("jdbc:oracle:oci8:" + constStruct.USERNAME + "/" + constStruct.PASSWORD + "@" + constStruct.DATABASE);
        Connection conn = ods.getConnection();

        // Query the employee names 
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery(sqlStmt);

        // Found the number of columns
        ResultSetMetaData rsmd = rset.getMetaData();
        numberOfColumns = rsmd.getColumnCount();

        columnTypes = new int[numberOfColumns + 1];
        columnTypeNames = new String[numberOfColumns + 1];

        //String c = new String(rsmd.getColumnType(1));
        for (int i = 1; i <= numberOfColumns; i++) {

            //c += " " + new String(rsmd.getColumnType(i));
            columnTypes[i] = rsmd.getColumnType(i);
            columnTypeNames[i] = rsmd.getColumnTypeName(i);
        }

        //queryRows = new String [constStruct.MAX_ROW_NUMBER];
        resultRows = new String[constStruct.MAX_MATCH_NUMBER][];

        int tmpRating;
        int j = 0, k = 0;
        String currentRow[];
        while (rset.next()) {

            //resultRows[j] = new String [numberOfColumns+1];
            //String r = new String(rset.getString(1));
            //String r1 = new String(r);
            //resultRows[j][1] = r;
            currentRow = new String[numberOfColumns + 1];
            currentRow[1] = new String(rset.getString(1));

            // Retrieve all columns of current row
            for (int i = 2; i <= numberOfColumns; i++) {
                currentRow[i] = rset.getString(i);
                //currentRow[i] = new String(rset.getString(1));
            }

            //out.println("CONSIDER row: " + j + " with " + currentRow[1] + " and city=" + currentRow[8]);
            tmpRating = computeACandidateRating(currentRow, out);

            //out.println("Back from computeAJobRating with tmpRating="+tmpRating);
            if (tmpRating > 0) {
                resultRows[j] = currentRow;
                rating[j++] = tmpRating;
            }
        }
        //close the result set, statement, and the connection
        rset.close();
        stmt.close();
        conn.close();

        numberOfMatchedRows = j;

        sortResultRows(out);

    } // End of doCandidateQuery 

    public void doInsert(String insertStmt)
    throws SQLException, IOException, ServletException {
        // Load Oracle driver
        //DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());

        //Connection conn = DriverManager.getConnection
        //     ("jdbc:oracle:oci:@gla92010", constStruct.USERNAME, constStruct.PASSWORD);

        OracleDataSource ods = new OracleDataSource();
        ods.setURL("jdbc:oracle:oci8:" + constStruct.USERNAME + "/" + constStruct.PASSWORD + "@" + constStruct.DATABASE);
        Connection conn = ods.getConnection();

        PreparedStatement stmt = conn.prepareStatement(insertStmt);

        stmt.executeUpdate(); //JDBC queues this for later execution

        ((OraclePreparedStatement) stmt).sendBatch(); // JDBC sends the queued request
        conn.commit();

        stmt.close();
        conn.close();
    } // End of doInsert

    public void printHeaderWithBodyTag(PrintWriter out, String bodyTag)
    throws IOException, ServletException {
        //out.println("Content-type: text/html");
        //out.println("");
        out.println("<html>");
        out.println(bodyTag);
    }

    public void printHeader(PrintWriter out)
    throws IOException, ServletException {
        //out.println("Content-type: text/html");
        //out.println("");
        out.println("<html>");
        out.println("<BODY BGCOLOR=\"#FFFFFF\">");
    }

    public void printJobQueryResultBody(PrintWriter out)
    throws IOException, ServletException {
        int k;

        out.println("<center>");
        out.println("<h3>Job Search Results</h3> (total " + numberOfMatchedRows + " matches)");
        out.println("<table >");
        //out.println("<th>jobId");
        out.println("<th>Rating");
        out.println("<th>Job Title");
        out.println("<th>Specialty");
        out.println("<th>Location");
        out.println("<th>Company");
        out.println("<th>Salary Range");

        for (int i = 0; i < numberOfMatchedRows; i++) {
            //out.println(rows[i]);

            k = originalIndex[i];
            out.println("<tr bgcolor=#ffffcc>");
            //out.println("<td>" + resultRows[k][1]);
            out.println("<td>" + rating[i]);
            //out.println("<td><a href=\"/peng/servlets/getjob?id=$count&filename=dummy\"" +
            //		"target=display>" + resultRows[k][3] + "</a>"); // job title
            out.println("<td><a href=\"/servlet/getJob?job_id=" +
                resultRows[k][1] + "\"" +
                "target=display>" + resultRows[k][3] + "</a>"); // job title
            out.println("<td>" + resultRows[k][4]); // specialty
            if (resultRows[k][8] != null) {
                out.print("<td>" + resultRows[k][8]); // city
                if (resultRows[k][7] != null) {
                    out.print(" (" + resultRows[k][7]); // state
                    if (resultRows[k][6] != null)
                        out.print(", " + resultRows[k][6]); // region
                    out.print(")");
                } else if (resultRows[k][6] != null)
                    out.print(" (" + resultRows[k][6] + ")"); // state
                out.println("");
            } else if (resultRows[k][7] != null) {
                out.print("<td>" + resultRows[k][7]); // state
                if (resultRows[k][6] != null) {
                    out.print(" (" + resultRows[k][6] + ")"); // state
                }
            } else
                out.println("<td>" + resultRows[k][6]); // region

            out.println("<td>" + resultRows[k][11]); // company name
            out.println("<td>" + resultRows[k][9] + "-" + resultRows[k][10]);
        }
        out.println("</tr>");
        out.println("</table></center>");
    } // End of printJobQueryResultBody(PrintWriter out)

    public void printCandidateQueryResultBody(PrintWriter out)
    throws IOException, ServletException {
        int k;

        out.println("<center>");
        out.println("<h3>Candidate Search Results</h3> (total " + numberOfMatchedRows + " matches)");
        out.println("<table >");
        //out.println("<th>jobId");
        out.println("<th>Rating");
        out.println("<th>Specialty");
        out.println("<th>Job Title");
        out.println("<th>Years of exp");
        out.println("<th>Degree");
        out.println("<th>Desired Salary");
        out.println("<th>Desired Job Location");

        for (int i = 0; i < numberOfMatchedRows; i++) {
            //out.println(rows[i]);

            k = originalIndex[i];
            out.println("<tr bgcolor=#ffffcc>");
            //out.println("<td>" + resultRows[k][1]);
            out.println("<td>" + rating[i]);
            out.println("<td><a href=\"/servlet/getCandidate?login_id=" +
                resultRows[k][1] + "\"" +
                "target=display>" + resultRows[k][5] + "</a>"); // specialty
            out.println("<td>" + resultRows[k][11]); // current job title
            out.print("<td>" + resultRows[k][14]); // year_of_exp
            out.print("<td>" + constStruct.degreeNames[Integer.parseInt(resultRows[k][15])]); // degree
            out.print("<td>" + resultRows[k][19]); // desired_salary
            if (resultRows[k][20] != null)
                out.print("<td>" + resultRows[k][20]); // desired_job_location
            else
                out.print("<td>" + "N/A");
        }
        out.println("</tr>");
        out.println("</table></center>");
    } // End of printCandidateQueryResultBody(PrintWriter out)

    public void printTailer(PrintWriter out)
    throws IOException, ServletException {

        out.println("<center>");
        out.println("<TABLE CELLSPACING=\"0\" CELLPADDING=\"3\" BORDER=\"0\">");
        out.println("<tr>");
        out.println("	<td> <A href=\"http://glaciers.cs.txstate.edu:8080/peng/jsp/home.jsp\"> Home </A>");
        out.println("	<td> <A href=\"http://glaciers.cs.txstate.edu:8080/peng/servlets/jobSearch.html\">Job Search </a>");
        out.println("	<td> <A href=\"/peng/servlets/employerLogin.html\">Employers </A>");
        out.println("	<td> <A href=\"/peng/servlets/candidateLogin.html\">Members </A>");
        out.println("</tr>");
        out.println("</table>");
        out.println("</center>");

        out.println("</body>");
        out.println("</html>");;
    }

    public void printTailerWithoutReference(PrintWriter out)
    throws IOException, ServletException {

        out.println("</body>");
        out.println("</html>");;
    }

    // Sort the list of output tuples in ascending order of their ratings.
    // For efficiency consideration, sorting is done in place -- only the
    // ratings array is rearranged and the order of elements of result_array
    // is unchanged. The array original_index keeps the index of the final
    // sorted order. That is, if original_index[i] == k, then the ith
    // output tuple is in the kth position of result_array.
    ///
    void sortResultRows(PrintWriter out) {
        int i, j, k;
        int maxIndex;


        //out.println( "In sortResultRows, total_matched_rows=" + numberOfMatchedRows);
        //for (i=0; i < numberOfMatchedRows; i++)
        //	out.println(i + ":" + rating[i]);

        for (i = 0; i < numberOfMatchedRows; i++) {

            //out.println( "Row " + i + "Rating = " + rating[i]);

            maxIndex = i;
            for (j = i + 1; j < numberOfMatchedRows; j++) {
                //out.println("Compare with row " + j + " with rating = " + rating[j]);

                if (rating[j] > rating[maxIndex]) {
                    //	out.println("swap: j=" + j + ",maxIndex=" + maxIndex + ",rating[j]=" + rating[j] +
                    //		"> rating[maxIndex]=" + rating[maxIndex]);
                    maxIndex = j;
                }
            }

            //out.println(maxIndex + "===" + i + " with " + rating[maxIndex] + "===" + rating[i]);

            k = originalIndex[maxIndex];
            originalIndex[maxIndex] = originalIndex[i];
            originalIndex[i] = k;

            k = rating[maxIndex];
            rating[maxIndex] = rating[i];
            rating[i] = k;
        }

        //for (i=0; i < numberOfMatchedRows; i++)
        //	out.println(i + "::" + originalIndex[i] + ":" + rating[i] +  "   " +
        //			originalIndex[i] + ":" + rating[originalIndex[i]]);
    } // End of sortResultRows

   
    public int computeACandidateRating(String curRow[], PrintWriter out)
    throws IOException, ServletException {
        int tmpRating = 100;
        int i;
        int sal;

        //out.println("Consider column 1 with paraValues[1]="+paraValues[1]);
        //out.println("Consider column 8 with paraValues[8]="+paraValues[8]);
        //out.println("constStruct.COLUMN_NUMBER_IN_MEMBER_TABLE="+constStruct.COLUMN_NUMBER_IN_MEMBER_TABLE);
        //out.println("locationSearchType = " + locationSearchType);
        for (i = 0; i < constStruct.COLUMN_NUMBER_IN_MEMBER_TABLE; i++) {
            if ((paraValues[i] != null) && !paraValues[i].equals("All") &&
                !paraValues[i].equals("Any")) {
                //out.println("CONSIDER column i="+i+" paraValues[i]="+paraValues[i] + "---");
                if ((i == constStruct.MEMBER_TABLE_YEAR_OF_EXP) && (curRow[i + 1] != null) &&
                    (askedYearOfExp > 0)) {
                    tmpRating -= computeYearOfExpRating(curRow[i + 1], out);
                } else if ((i == constStruct.MEMBER_TABLE_DEGREE) && (curRow[i + 1] != null) &&
                    (askedDegree > -1)) {
                    tmpRating -= computeDegreeRating(curRow[i + 1], out);
                    /* Search by region */
                } else if ((i == constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION_CODE) &&
                    (locationSearchType == constStruct.SEARCH_BY_REGION)) {
                    tmpRating -= computeRegionRating(curRow, out);
                    /* Search by state */
                } else if ((i == constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION_CODE) &&
                    (locationSearchType == constStruct.SEARCH_BY_STATE)) {
                    tmpRating -= computeStateRating(askedLocationIndex, curRow, out);
                    /* Search by city */
                } else if ((i == constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION) &&
                    (locationSearchType == constStruct.SEARCH_BY_CITY)) {
                    tmpRating -= computeCityRating(askedLocationIndex, curRow, out);
                    // salary
                } else if ((i == constStruct.MEMBER_TABLE_DESIRED_SALARY) && (askedMaxSal != 0) &&
                    (curRow[i + 1] != null)) {
                    sal = Integer.parseInt(curRow[i + 1]);
                    //out.println("Check salary for row " + i + " with sal = " + sal);
                    tmpRating -= computeSalRating(sal);
                }

                // un-qualified job entry
                if (tmpRating <= 0)
                    break;
            }
        }

        return tmpRating;
    } // End of computeACandidateRating()

    /*
     *  Compute the rating based on the region information requested by a user.
     *  The parameter oneRow contains a tuple retrieved from the job table that
     *		satisfies basic requirement.
     *  Value returned:  an integer between 0 and 100. 0 means perfect match,
     *		100 means no matching at all.  The method computeAJobRating()
     *		will deduct the returned value from its final rating.
     */
    public int computeRegionRating(String oneRow[], PrintWriter out)
    throws IOException, ServletException {
        int deduction = 100, tmpDeduction = 100;

        int regionIndex = -1, stateIndex = -1, cityIndex = -1, locationIndex = -1;
        String regionName, stateName, cityName, locationName, s;
        int locationCode;

        // Job search
        if (searchType == constStruct.JOB_SEARCH) {
            if (oneRow[constStruct.JOB_TABLE_REGION_NAME + 1] != null) {
                regionName = new String(oneRow[constStruct.JOB_TABLE_REGION_NAME + 1]);
                regionIndex = findRegionIndex(regionName.toLowerCase());
            }
            if (oneRow[constStruct.JOB_TABLE_STATE_NAME + 1] != null) {
                stateName = new String(oneRow[constStruct.JOB_TABLE_STATE_NAME + 1]);
                stateIndex = findStateIndex(stateName.toLowerCase());
            }
            if (oneRow[constStruct.JOB_TABLE_LOCATION + 1] != null) {
                cityName = new String(oneRow[constStruct.JOB_TABLE_LOCATION + 1]);
                cityIndex = findCityIndex(cityName.toLowerCase());
            }

            if (regionIndex != -1) {
                // perfect match, or asked region is contained in the job region
                if (constStruct.regionCompability[regionIndex][askedLocationIndex] == 1)
                    deduction = 0;
                else if (constStruct.regionCompability[regionIndex][askedLocationIndex] == 2) {
                    //out.println("THE GIVEN ROW has a region " + constStruct.usRegions[regionIndex] + 
                    //	  	" that might be compatible with asked region " +
                    //			constStruct.usRegions[askedLocationIndex]);
                    deduction = 70;
                    if (stateIndex != -1)
                        tmpDeduction = checkIfStateInRegion(stateIndex, askedLocationIndex, out);
                    else if (cityIndex != -1)
                        tmpDeduction = checkIfCityInRegion(cityIndex, askedLocationIndex, out);
                    deduction = Math.min(deduction, tmpDeduction);
                }
            } else if (stateIndex != -1) {
                //out.println("THE GIVEN ROW has a state " + constStruct.usStates[stateIndex] + 
                //	  	" that might be in asked region " +
                //		constStruct.usRegions[askedLocationIndex]);
                deduction = checkIfStateInRegion(stateIndex, askedLocationIndex, out);
            } else if (cityIndex != -1) {
                // out.println("THE GIVEN ROW has a city " + constStruct.usCities[cityIndex] + 
                //		  	" that might be in asked region " +
                //			constStruct.usRegions[askedLocationIndex]);
                deduction = checkIfCityInRegion(cityIndex, askedLocationIndex, out);
            }
            // Member search
        } else if (searchType == constStruct.MEMBER_SEARCH) {
            if (oneRow[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION + 1] != null) {
                locationName = new String(oneRow[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION + 1]);
                s = new String(oneRow[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION_CODE + 1]);
                locationCode = Integer.parseInt(s);

                if (askedLocationIndex == -1) // search for "Any"
                    return 0;
                if (locationCode == 2) {
                    locationIndex = findRegionIndex(locationName.toLowerCase());
                    if (locationIndex < 0) {
                        out.println("locationName=" + locationName + ",locationIndex=" + locationIndex);
                    } else if (constStruct.regionCompability[locationIndex][askedLocationIndex] == 1)
                        deduction = 0;
                    else if (constStruct.regionCompability[locationIndex][askedLocationIndex] == 2) {
                        //out.println("THE GIVEN ROW has a region " + constStruct.usRegions[regionIndex] + 
                        //	  	" that might be compatible with asked region " +
                        //			constStruct.usRegions[askedLocationIndex]);
                        deduction = 70;
                    }
                } else if (locationCode == 3) {
                    locationIndex = findStateIndex(locationName.toLowerCase());
                    deduction = checkIfStateInRegion(locationIndex, askedLocationIndex, out);
                } else if (locationCode == 4) {
                    locationIndex = findCityIndex(locationName.toLowerCase());
                    deduction = checkIfCityInRegion(locationIndex, askedLocationIndex, out);
                }
            }
            // C_G search
        } else if (searchType == constStruct.C_G_SEARCH) {
            if (oneRow[constStruct.C_G_TABLE_DESIRED_JOB_LOCATION + 1] != null) {
                locationName = new String(oneRow[constStruct.C_G_TABLE_DESIRED_JOB_LOCATION + 1]);
                locationIndex = findCityIndex(locationName);
                // Details to be filled
            }
        } else {
            out.println("Unknown Search Type!");
            deduction = 100;
        }

        return deduction;
        //out.println("Calling computeRegionRating");

    } // End of computeRegionRating

    public int checkIfStateInRegion(int stateIndex, int regionIndex, PrintWriter out)
    throws IOException, ServletException {
        int deduction = 100;

        //out.println("In checkIfStateInRegion for state " +constStruct.usStates[stateIndex]+ " and " +
        //		"region " + constStruct.usRegions[regionIndex]);
        if (theStateIsInTheRegion(stateIndex, regionIndex)) {
            //out.println("SUCCESS: The state is in the region " + constStruct.usRegions[regionIndex]);
            deduction = 0;
        }
        return deduction;
    }

    public int checkIfCityInState(int cityIndex, int stateIndex, PrintWriter out)
    throws IOException, ServletException {
        int deduction = 100;

        //out.println("In checkIfCityInState for city " +constStruct.usCities[cityIndex]+ " and " +
        //		"state " + constStruct.usStates[stateIndex]);
        if (theCityIsInTheState(cityIndex, stateIndex)) {
            //out.println("SUCCESS: The city is in the state " + constStruct.usStates[stateIndex]);
            deduction = 0;
        }
        return deduction;
    }

    public int checkIfCityInRegion(int cityIndex, int regionIndex, PrintWriter out)
    throws IOException, ServletException {
        int deduction = 100;

        int r = usStateInRegionBeginningIndex[regionIndex], s, l, st;

        //out.println("In checkIfCityInRegion for city " +constStruct.usCities[cityIndex]+ " and " +
        //		"region " + constStruct.usRegions[regionIndex] + ", beginning index=" + r);
        for (int i = r; constStruct.usStatesInRegions[i] != constStruct.END_SUB_LIST; i++) {
            st = constStruct.usStatesInRegions[i];
            s = usCityInStateBeginningIndex[st];
            l = constStruct.usCitiesInStatesNumber[st];
            //out.println("i=" + i + ", s=" + s + ", l=" + l + ", st=" + st);
            //out.println("Check state " + constStruct.usStates[st]);
            for (int j = 0; j < l; j++) {
                //out.println("Check city " + constStruct.usCities[constStruct.usCitiesInStates[j+s]] +
                //" with city index " + constStruct.usCitiesInStates[j+s]);
                if (theCityIsInTheState(cityIndex, st)) {
                    //out.println("SUCCESS: The city is in the state " +
                    //		constStruct.usStates[st]);
                    deduction = 0;
                }
            }
        }
        return deduction;
    }

    /*
     *  Compute the rating based on the state information specified by a user.
     *  The parameter askedStateIndex is the index of the state the search is looking for.
     *  The parameter oneRow contains a tuple retrieved from the job table that
     *	  satisfies basic requirement. The one_row_ind is the corresponding
     *	  indicate array.
     *  Value returned:  an integer between 0 and 100. 0 means perfect match,
     *		100 means no matching at all.  The method computeAJobRating()
     *		will deduct the returned value from its final rating.
     */
    public int computeStateRating(int askedStateIndex, String oneRow[], PrintWriter out)
    throws IOException, ServletException {
        int deduction = 100, tmpDeduction = 100;

        int i, j, k;
        int regionIndex = -1, stateIndex = -1, cityIndex = -1, locationIndex = -1;
        String regionName, stateName, cityName, locationName, s;
        int locationCode;

        //out.println("Calling computeStateRating");
        if (searchType == constStruct.JOB_SEARCH) {
            if (oneRow[constStruct.JOB_TABLE_REGION_NAME + 1] != null) {
                regionName = new String(oneRow[constStruct.JOB_TABLE_REGION_NAME + 1]);
                regionIndex = findRegionIndex(regionName.toLowerCase());
            }
            if (oneRow[constStruct.JOB_TABLE_STATE_NAME + 1] != null) {
                stateName = new String(oneRow[constStruct.JOB_TABLE_STATE_NAME + 1]);
                stateIndex = findStateIndex(stateName.toLowerCase());
            }
            if (oneRow[constStruct.JOB_TABLE_LOCATION + 1] != null) {
                cityName = new String(oneRow[constStruct.JOB_TABLE_LOCATION + 1]);
                cityIndex = findCityIndex(cityName.toLowerCase());
            }
            if (stateIndex != -1) {

                //out.println("Asked state index is " + askedStateIndex);
                //out.println("Asked state name is " + constStruct.usStates[askedStateIndex]);

                deduction = findNeighborStateRating(stateIndex, askedStateIndex, out);
            } else if (cityIndex != -1) { // The row does not have a valid state, but has a valid city

                deduction = findCityInStateRating(cityIndex, askedStateIndex, out);
            } else if (regionIndex != -1) { // The row does not have a valid state or city,
                //    but does have a valid region name
                deduction = 30; // by default, the rating deduction is 30% for this case
                tmpDeduction = checkIfStateInRegion(askedStateIndex, regionIndex, out);
                deduction = Math.max(deduction, tmpDeduction);
            }
            // Member search
        } else if (searchType == constStruct.MEMBER_SEARCH) {
            if (oneRow[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION + 1] != null) {
                locationName = new String(oneRow[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION + 1]);
                s = new String(oneRow[constStruct.MEMBER_TABLE_DESIRED_JOB_LOCATION_CODE + 1]);
                locationCode = Integer.parseInt(s);

                if (locationCode == 2) {
                    locationIndex = findRegionIndex(locationName.toLowerCase());
                    deduction = checkIfStateInRegion(askedStateIndex, locationIndex, out);
                } else if (locationCode == 3) {
                    locationIndex = findStateIndex(locationName.toLowerCase());

                    j = neighborStateBeginningIndex[askedStateIndex];
                    k = constStruct.usNeighborStateNumber[askedStateIndex];

                    //out.println( "beginning state index = " + j + " and # of neighbors is ");
                    for (i = 0; i < 2 * k; i = i + 2) {
                        //out.println ("Consider state with " + j+i +
                        //	" as index in usNeighborStateRatingList and " +
                        //	"[j+i]=" +  constStruct.usNeighborStateRatingList[j+i]);
                        //out.println ("The state name should be " +
                        //		constStruct.usStates[constStruct.usNeighborStateRatingList[j+i]]);

                        if (constStruct.usNeighborStateRatingList[j + i] == locationIndex) {
                            deduction = constStruct.usNeighborStateRatingList[j + i + 1];
                            break;
                        }
                    }
                } else if (locationCode == 4) {
                    locationIndex = findCityIndex(locationName.toLowerCase());
                    deduction = checkIfCityInState(locationIndex, askedStateIndex, out);
                }
            }
            // Member search
        } else {
            out.println("Unknown Search Type!");
            deduction = 100;
        }

        return deduction;
    } // End of computeStateIndex


    /*
     *  Compute the rating based on the city information specified by a user.
     *  The parameter oneRow contains a tuple retrieved from the job table that
     *		satisfies basic requirement. The one_row_ind is the corresponding
     *		indicate array
     *  Value returned:  an integer between 0 and 100. 0 means perfect match,
     *		100 means no matching at all.  The method computeAJobRating()
     *		will deduct the returned value from its final rating.
     */
    public int computeCityRating(int askedCityIndex, String oneRow[], PrintWriter out)
    throws IOException, ServletException {
        int deduction = 100, tmpDeduction = 100;

        int i, j, k;
        int cityIndex = -1;
        int stateIndex = -1;
        int regionIndex = -1;
        int askedStateIndex, askedRegionIndex;

        String cityName, askedCityName;
        String stateName, regionName;


        if (searchType == constStruct.JOB_SEARCH) {
            if (oneRow[constStruct.JOB_TABLE_REGION_NAME + 1] != null) {
                regionName = new String(oneRow[constStruct.JOB_TABLE_REGION_NAME + 1]);
                regionIndex = findRegionIndex(regionName.toLowerCase());
            }
            if (oneRow[constStruct.JOB_TABLE_STATE_NAME + 1] != null) {
                stateName = new String(oneRow[constStruct.JOB_TABLE_STATE_NAME + 1]);
                stateIndex = findStateIndex(stateName.toLowerCase());
            }
            if (oneRow[constStruct.JOB_TABLE_LOCATION + 1] != null) {
                cityName = new String(oneRow[constStruct.JOB_TABLE_LOCATION + 1]);
                cityIndex = findCityIndex(cityName.toLowerCase());
            }


            if (cityIndex != -1) { // The row does not have a valid state, but has a valid city
                deduction = findNeighborCityRating(cityIndex, askedCityIndex, out);
            } else if (stateIndex != -1) {
                askedStateIndex = findTheStateContainingTheCity(askedCityIndex);
                if (stateIndex == askedStateIndex)
                    deduction = 35;
                else
                    deduction = 10 + findNeighborStateRating(stateIndex, askedStateIndex, out);

            } else if (regionIndex != -1) { // The row does not have a valid state or city,                                                //    but does have a valid region name
                askedStateIndex = findTheStateContainingTheCity(askedCityIndex);
                if (checkIfStateInRegion(askedStateIndex, regionIndex, out) == 0)
                    deduction = 50;
            }

        } else {
            out.println("<pre>Error: unknown search type.</pre>");
        }
        return deduction;

    } // End of computeCityRating()
 
  
 
    public int findNeighborStateRating(int stateIndex, int askedStateIndex, PrintWriter out)
    throws IOException, ServletException {
        int deduction = 100;
        int i, j, k;

        out.println("Asked state index is " + askedStateIndex);
        out.println(", asked state name is " + constStruct.usStates[askedStateIndex] + "<br>");

        j = neighborStateBeginningIndex[askedStateIndex];
        k = constStruct.usNeighborStateNumber[askedStateIndex];

        out.println("beginning state index = " + j + " and # of neighbors is " + k + "<br>");
        for (i = 0; i < 2 * k; i = i + 2) {
            out.println("State " + constStruct.usStates[constStruct.usNeighborStateRatingList[i + j]] +
                " with index " + constStruct.usNeighborStateRatingList[i + j] + "<br>");
            if (constStruct.usNeighborStateRatingList[i + j] == stateIndex) {
                deduction = constStruct.usNeighborStateRatingList[j + i + 1];
                break;
            }
        }
        return deduction;
    } // End of findNeighborStateRating

    public int findCityInStateRating(int cityIndex, int askedStateIndex, PrintWriter out) {
        int deduction = 100;
        int i, j, k;
        int stateIndex;

        j = neighborStateBeginningIndex[askedStateIndex];
        k = constStruct.usNeighborStateNumber[askedStateIndex];

        for (i = 0; i < 2 * k; i += 2) {

            stateIndex = constStruct.usNeighborStateRatingList[j + i];
            if (theCityIsInTheState(cityIndex, stateIndex)) {
                deduction = constStruct.usNeighborStateRatingList[j + i + 1];
                break;
            }
        }

        return deduction;
    }

    public int findNeighborCityRating(int cityIndex, int askedCityIndex, PrintWriter out) {
        int i, j, k;
        int deduction = 100;

        j = neighborCityBeginningIndex[askedCityIndex];

        k = constStruct.usNeighborCityNumber[askedCityIndex];


        for (i = 0; i < 2 * k; i = i + 2) {

            out.println("Check " + constStruct.usCities[constStruct.usNeighborCityRatingList[j + i]] + "<br>\n");
            if (constStruct.usNeighborCityRatingList[j + i] == cityIndex) {
                deduction = constStruct.usNeighborCityRatingList[j + i + 1];
                break;
            }

        }
        return deduction;
    }

    public int computeSalRating(int sal)
    throws IOException, ServletException {
        int deduction = 0;
        float percent = 0;
        boolean calDeduction = false;

        if (searchType == constStruct.JOB_SEARCH) {
            percent = (((float)(askedMinSal - sal)) / ((float)(askedMinSal + 1)));
            calDeduction = askedMinSal > sal;
        } else if (searchType == constStruct.MEMBER_SEARCH) {
            percent = (((float)(sal - askedMaxSal)) / ((float)(askedMaxSal + 1)));
            calDeduction = sal > askedMaxSal;
        }

        if (calDeduction) {
            //percent = (((float)(askedMinSal - sal)) / ((float)(askedMinSal + 1)));

            if (percent <= 0.05) // salary is 95% or above
                deduction = 5;
            else if (percent <= 0.10) // salary is between 90% and 95%
                deduction = 10;
            else if (percent <= 0.15) // salary is between 85% and 90%
                deduction = 15;
            else if (percent <= 0.20) // salary is between 80% and 85%
                deduction = 20;
            else if (percent <= 0.25) // salary is between 75% and 80%
                deduction = 25;
            else if (percent <= 0.30) // salary is between 70% and 75% 
                deduction = 30;
            else if (percent <= 0.35) // salary is between 65% and 70%
                deduction = 35;
            else if (percent <= 0.40) // salary is between 60% and 65%
                deduction = 40;
            else if (percent <= 0.45) // salary is between 55% and 60%
                deduction = 45;
            else if (percent <= 0.50) // salary is between 50% and 55%
                deduction = 50;
            else if (percent <= 0.55) // salary is between 45% and 50%
                deduction = 55;
            else if (percent <= 0.60) // salary is between 40% and 45%
                deduction = 60;
            else if (percent <= 0.65) // salary is between 35% and 40%
                deduction = 65;
            else deduction = 70; // salary is at most 35%
        }
        return deduction;
    } // End of computeSalRating()


    int computeYearOfExpRating(String yearOfExp, PrintWriter out)
    throws IOException, ServletException {
        int deduction = 0, diff;
        int givenYearOfExp = Integer.parseInt(yearOfExp);

        diff = askedYearOfExp - givenYearOfExp;

        out.println("askedYearOfExp=" + askedYearOfExp + ", givenYearOfExp=" + givenYearOfExp);
        if (diff > 0) {
            if (diff <= 1)
                deduction = 10;
            else if (diff <= 2)
                deduction = 20;
            else if (diff <= 3)
                deduction = 30;
            else if (diff <= 4)
                deduction = 40;
            else
                deduction = 50;
        }
        return deduction;
    }


    int computeDegreeRating(String degree, PrintWriter out)
    throws IOException, ServletException {
        int deduction = 0, diff;
        int givenDegree = Integer.parseInt(degree);

        diff = askedDegree - givenDegree;

        out.println("askedDegree=" + askedDegree + ", givenDegree=" + givenDegree);
        if (diff > 0) {
            if (diff <= 1)
                deduction = 5;
            else if (diff <= 2)
                deduction = 10;
            else if (diff <= 3)
                deduction = 15;
            else if (diff <= 4)
                deduction = 20;
            else
                deduction = 25;
        }
        return deduction;
    }


     
    int findJobTitleIndex(String a_name) {
        for (int i = 0; i < constStruct.NUMBER_OF_JOB_TITLES; i++)
            if (a_name.compareTo(constStruct.jobTitleList[i]) == 0)
                return i;
        return -1;
    }

    int findJobSpecIndex(String a_name) {
        for (int i = 0; i < constStruct.NUMBER_OF_JOB_SPECS; i++)
            if (a_name.compareTo(constStruct.specialtyAreaList[i]) == 0)
                return i;
        return -1;
    }
 
    int findRegionIndex(String a_name) {
        for (int i = 0; i < constStruct.NUMBER_OF_US_REGIONS; i++)
            if (a_name.compareTo(constStruct.usRegions[i]) == 0)
                return i;
        return -1;
    }


    int findStateIndex(String a_name) {
        for (int i = 0; i < constStruct.NUMBER_OF_US_STATES; i++)
            if (a_name.compareTo(constStruct.usStates[i]) == 0)
                return i;
        return -1;

    }


    int findCityIndex(String a_name) {

        for (int i = 0; i < constStruct.NUMBER_OF_US_CITIES; i++)
            if (a_name.compareTo(constStruct.usCities[i]) == 0)
                return i;
        return -1;

    }


    boolean theStateIsInTheRegion(int stateIndx, int regionIndx) {
        int k;

        k = usStateInRegionBeginningIndex[regionIndx];
        //limit = us_states_in_regions_number[region_indx];

        for (int i = 0; constStruct.usStatesInRegions[k + i] >= 0; i++) {

            if (stateIndx == constStruct.usStatesInRegions[k + i]) {
                return true;
            }
        }

        return false;
    }


    boolean theCityIsInTheState(int cityIndx, int stateIndx) {
        int limit, k;

        k = usCityInStateBeginningIndex[stateIndx];
        limit = constStruct.usCitiesInStatesNumber[stateIndx];

        for (int i = 0; i < limit; i++) {
            if (cityIndx == constStruct.usCitiesInStates[k + i])
                return true;
        }
        return false;
    }

    public int findTheStateContainingTheCity(int cityIndex) {
        int k, limit;

        for (int i = 0; i < constStruct.NUMBER_OF_US_STATES; i++) {

            k = usCityInStateBeginningIndex[i];
            limit = constStruct.usCitiesInStatesNumber[i];
            for (int j = 0; j < limit; j++)
                if (cityIndex == constStruct.usCitiesInStates[k + j])
                    return i;
        }

        return -1;
    }

    public int getANewJobId()
    throws SQLException, IOException {

        int maxJobId;

        //Connection conn = DriverManager.getConnection
        //   ("jdbc:oracle:oci:@gla92010", constStruct.USERNAME, constStruct.PASSWORD);

        OracleDataSource ods = new OracleDataSource();
        ods.setURL("jdbc:oracle:oci8:" + constStruct.USERNAME + "/" + constStruct.PASSWORD + "@" + constStruct.DATABASE);
        Connection conn = ods.getConnection();

        // Query the employee names 
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery("SELECT MAX(job_id) FROM job_ids");

        // Found the number of columns
        rset.next();
        maxJobId = rset.getInt(1);

        rset.close();
        stmt.close();

        maxJobId += 1;

        // update the table job_ids
        PreparedStatement instStmt = conn.prepareStatement("INSERT INTO job_ids VALUES(" + maxJobId + ")");

        instStmt.executeUpdate(); //JDBC queues this for later execution

        ((OraclePreparedStatement) instStmt).sendBatch(); // JDBC sends the queued request
        instStmt.close();
        conn.commit();

        return maxJobId;
    }

    // Test if the given empId or loginId is unique in the employer or member table
    public boolean idIsUnique(String id, int tableNameIndex)
    throws SQLException, IOException {

        String givenId, anId;
        boolean isUnique = true;

        String queryStmt;

        //Connection conn = DriverManager.getConnection
        //   ("jdbc:oracle:oci:@gla92010", constStruct.USERNAME, constStruct.PASSWORD);

        OracleDataSource ods = new OracleDataSource();
        ods.setURL("jdbc:oracle:oci8:" + constStruct.USERNAME + "/" + constStruct.PASSWORD + "@" + constStruct.DATABASE);
        Connection conn = ods.getConnection();

        // Query the employee names 
        if (tableNameIndex == constStruct.MEMBER_TABLE_INDEX)
            queryStmt = new String("SELECT login_id FROM " + constStruct.tableNames[tableNameIndex - 1]);
        else if (tableNameIndex == constStruct.EMPLOYER_TABLE_INDEX)
            queryStmt = new String("SELECT emp_id FROM " + constStruct.tableNames[tableNameIndex - 1]);
        else
            return false;
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery(queryStmt);

        //anEmpId = new char[constStruct.MAX_COLUMN_NAME_LENGTH];
        givenId = id.toLowerCase();

        while (rset.next()) {
            anId = rset.getString(1);
            if (givenId.equals(anId.toLowerCase())) {
                isUnique = false;
                break;
            }
        }

        rset.close();
        stmt.close();

        return isUnique;
    }
	
	
	
	
	/*********************************** Extended ***********************************/
    /*********************************** Extended ***********************************/
	/*********************************** Extended ***********************************/	
	
	 public int computeAJobRating(String curRow[], PrintWriter out)
    throws IOException, ServletException {
        int tmpRating = 100;
        int i;
        int sal;
 
        for (i = 0; i < constStruct.COLUMN_NUMBER_IN_JOB_TABLE; i++) {
			
			// Checking if paraValues[i] is a valid value
            if ((paraValues[i] != null) && !paraValues[i].equals("All") && !paraValues[i].equals("Any")) {
                 
				 
				// Check for location and salary range
                if ((i == constStruct.JOB_TABLE_REGION_NAME) && (locationSearchType == constStruct.SEARCH_BY_REGION)) {
                    tmpRating -= computeRegionRating(curRow, out);
                } /* Search by state */
				
                else if ((i == constStruct.JOB_TABLE_STATE_NAME) && (locationSearchType == constStruct.SEARCH_BY_STATE)) {
                    tmpRating -= computeStateRating(askedLocationIndex, curRow, out);
                } /* Search by city */
				
                else if ((i == constStruct.JOB_TABLE_LOCATION) && (locationSearchType == constStruct.SEARCH_BY_CITY)) {
                    tmpRating -= computeCityRating(askedLocationIndex, curRow, out);
                } // salary
				
                else if ((i == constStruct.JOB_TABLE_MIN_SALARY) && (askedMinSal != 0) && (curRow[i + 1] != null)) {
                    sal = Integer.parseInt(curRow[i + 1]);
                    //out.println("Check salary for row " + i + " with sal = " + sal);
                    tmpRating -= computeSalRating(sal);
                }
				
				
				// Check for job title and job specification
				else if ((i == constStruct.JOB_TABLE_JOB_TITLE)) {
					  
					 String jobTitle_check = curRow[i + 1];  
					  
                     tmpRating -= computeJobTitleRating(jobTitle_check, out);
			 
			    }  
				
				 
  				// Check for job specification and job specification
				else if ((i == constStruct.JOB_TABLE_SPECIALIZATION)) {
					  
                     String jobSpec_check = curRow[i + 1];  
					  
                     tmpRating -= computeJobSpecRating(jobSpec_check, out);
                 
			    }  
      
				// keyword rating
				else if ((i== constStruct.JOB_TABLE_DESCRIPTION) && paraValues[constStruct.JOB_TABLE_DESCRIPTION].length() > 0) {
					 
					tmpRating -= computeKeywordRating(paraValues[constStruct.JOB_TABLE_DESCRIPTION], curRow, out);
				}
 
                if (tmpRating <= 0)  break;
            }
	 
        }

        return tmpRating;
		
    } // End of computeAJobRating()
 
 
    /********* Job Title Rating *********/
	public int computeJobTitleRating(String jobTitle_check, PrintWriter out) throws IOException, ServletException {
		 
		 String targetJobTitle = paraValues[constStruct.JOB_TABLE_JOB_TITLE]; 
		   
		 int a = findJobTitleIndex(targetJobTitle); 
		  
		 int b = findJobTitleIndex(jobTitle_check); 
		  
		 int[][] jobTitleMatrix = constStruct.jobTitleRatingList; 
		 
		 int rating = jobTitleMatrix[a][b]; 
		   
		 return rating; 
	}
	
	 
    /********* Job Spec Rating *********/
    public int computeJobSpecRating(String jobSpec_check, PrintWriter out) throws IOException, ServletException {
		  
	      String targetJobSpec = paraValues[constStruct.JOB_TABLE_SPECIALIZATION]; 
		  
		  int rating = 0; 
		  
		  if( targetJobSpec.trim() == jobSpec_check.trim() ) {
			rating = 100;  
		  } else {
		 
				boolean overLap = false;    
				   
				String[] targetSplit = targetJobSpec.split("\\s+");   
				
				String[] currentSplit = jobSpec_check.split("\\s+");   
				   
				   
				for(String a: targetSplit){
					
					for(String b:currentSplit){
						
						if(a == b) overLap = true; 
					}
				}
				
				if(overLap) rating = 30;
		     	else rating = 0;    
		  }
		  
		  return rating;
	}
 
	   
	public int computeKeywordRating(String keyword, String oneRow[], PrintWriter out) throws IOException, ServletException {
		
		boolean foundInDescription = false;
		boolean foundInSpec = false;
 
		// if description is not empty 
		if ((oneRow[constStruct.JOB_TABLE_DESCRIPTION + 1] != null) &&
			oneRow[constStruct.JOB_TABLE_DESCRIPTION + 1].toLowerCase().contains(keyword.toLowerCase())) {
				
			// highlight each keyword
			oneRow[constStruct.JOB_TABLE_DESCRIPTION + 1] = oneRow[constStruct.JOB_TABLE_DESCRIPTION + 1].replaceAll("(?i)" 
																+ keyword, "<font color=\"red\"><b>" 
																+ keyword + "</b></font>");
			foundInDescription = true;
		}
		
		// if specification is not empty  
		if ((oneRow[constStruct.JOB_TABLE_SPECIALIZATION + 1] != null) && oneRow[constStruct.JOB_TABLE_SPECIALIZATION + 1].toLowerCase().contains(keyword.toLowerCase())) {
				
			// highlight each  keyword
			oneRow[constStruct.JOB_TABLE_SPECIALIZATION + 1] =
				oneRow[constStruct.JOB_TABLE_SPECIALIZATION + 1].replaceAll("(?i)" + keyword, "<font color=\"#01DF01\"><b>" + keyword + "</b></font>");
			foundInSpec = true;
		}
		
		
	    // if job title is not empty  
		if ((oneRow[constStruct.JOB_TABLE_JOB_TITLE + 1] != null) && oneRow[constStruct.JOB_TABLE_JOB_TITLE + 1].toLowerCase().contains(keyword.toLowerCase())) {
				
			// highlight each  keyword
			oneRow[constStruct.JOB_TABLE_JOB_TITLE + 1] =
				oneRow[constStruct.JOB_TABLE_JOB_TITLE + 1].replaceAll("(?i)" + keyword, "<font color=\"#210B61\"><b>" + keyword + "</b></font>");
			foundInSpec = true;
		}
		
		
	    // if job title is not empty  
		if ((oneRow[constStruct.JOB_TABLE_JOB_TITLE + 1] != null) && oneRow[constStruct.JOB_TABLE_JOB_TITLE + 1].toLowerCase().contains(keyword.toLowerCase())) {
				
			// highlight each  keyword
			oneRow[constStruct.JOB_TABLE_JOB_TITLE + 1] =
				oneRow[constStruct.JOB_TABLE_JOB_TITLE + 1].replaceAll("(?i)" + keyword, "<font color=\"red\"><b>" + keyword + "</b></font>");
			foundInSpec = true;
		}
		
	    // if JOB_TABLE_REGION_NAME is not empty  
		if ((oneRow[constStruct.JOB_TABLE_REGION_NAME + 1] != null) && oneRow[constStruct.JOB_TABLE_REGION_NAME + 1].toLowerCase().contains(keyword.toLowerCase())) {
				
			// highlight each  keyword
			oneRow[constStruct.JOB_TABLE_REGION_NAME + 1] =
				oneRow[constStruct.JOB_TABLE_REGION_NAME + 1].replaceAll("(?i)" + keyword, "<font color=\"#FF8000\"><b>" + keyword + "</b></font>");
			foundInSpec = true;
		}
		
		  
	    // if JOB_TABLE_CITY is not empty  
		if ((oneRow[constStruct.JOB_TABLE_CITY + 1] != null) && oneRow[constStruct.JOB_TABLE_CITY + 1].toLowerCase().contains(keyword.toLowerCase())) {
				
			// highlight each  keyword
			oneRow[constStruct.JOB_TABLE_CITY + 1] =
				oneRow[constStruct.JOB_TABLE_CITY + 1].replaceAll("(?i)" + keyword, "<font color=\"red\"><b>" + keyword + "</b></font>");
			foundInSpec = true;
		}
		
	    // if JOB_TABLE_STATE_NAME is not empty  
		if ((oneRow[constStruct.JOB_TABLE_STATE_NAME + 1] != null) && oneRow[constStruct.JOB_TABLE_STATE_NAME + 1].toLowerCase().contains(keyword.toLowerCase())) {
				
			// highlight each  keyword
			oneRow[constStruct.JOB_TABLE_STATE_NAME + 1] =
				oneRow[constStruct.JOB_TABLE_STATE_NAME + 1].replaceAll("(?i)" + keyword, "<font color=\"#DF013A\"><b>" + keyword + "</b></font>");
			foundInSpec = true;
		}
		
		 // if JOB_TABLE_CONTACT_PERSON is not empty  
		if ((oneRow[constStruct.JOB_TABLE_CONTACT_PERSON + 1] != null) && oneRow[constStruct.JOB_TABLE_CONTACT_PERSON + 1].toLowerCase().contains(keyword.toLowerCase())) {
				
			// highlight each  keyword
			oneRow[constStruct.JOB_TABLE_CONTACT_PERSON + 1] =
				oneRow[constStruct.JOB_TABLE_CONTACT_PERSON + 1].replaceAll("(?i)" + keyword, "<font color=\"#01DF01\"><b>" + keyword + "</b></font>");
			foundInSpec = true;
		}
		
	    // if JOB_TABLE_COMPANY_NAME is not empty  
		if ((oneRow[constStruct.JOB_TABLE_COMPANY_NAME + 1] != null) && oneRow[constStruct.JOB_TABLE_COMPANY_NAME + 1].toLowerCase().contains(keyword.toLowerCase())) {
				
			// highlight each  keyword
			oneRow[constStruct.JOB_TABLE_COMPANY_NAME + 1] =
				oneRow[constStruct.JOB_TABLE_COMPANY_NAME + 1].replaceAll("(?i)" + keyword, "<font color=\"#FF8000\"><b>" + keyword + "</b></font>");
			foundInSpec = true;
		}
		 
		int rating = 100;
		
		if (foundInDescription) {  
			rating = 20;
		} else if (foundInSpec) {  
			rating = 55;
		}
	 
		return rating;
	}
	
	
	
} 