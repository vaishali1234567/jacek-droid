package pl.looksok.activity.calcresult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import pl.looksok.R;
import pl.looksok.logic.CalculationLogic;
import pl.looksok.logic.PersonData;
import pl.looksok.utils.CalculationPrinter;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

public class CalcResultUtils {

	private static final String LOG_TAG = CalcResultUtils.class.getSimpleName();

	private String[] getEmailsArray(Context context, CalculationLogic calc) {
		HashMap<String, PersonData> calcResult = calc.getCalculationResult();
		String [] emails = new String[calcResult.size()];
		boolean noEmailsProvidedInfoShown = false;
		boolean isAnyEmailAddedToArray = false;

		Iterator<String> it = calcResult.keySet().iterator();
		int i = 0;
		while (it.hasNext()){
			String key = it.next();
			try{
				HashSet<String> emailsSet = calcResult.get(key).getEmails();
				if(emailsSet.size()==0)
					emails[i] = "";
				else{
					Iterator<String> it2 = emailsSet.iterator();
					while (it2.hasNext()){
						String email = it2.next();
						emails[i] = email; 
						isAnyEmailAddedToArray = true;
						break;
					}
				}
			}catch(NullPointerException e){
				Log.d(LOG_TAG, "Unknown email address for person '" + key + "'. " + e.getMessage());
				if(!noEmailsProvidedInfoShown){
					Toast.makeText(context, R.string.email_utils_error_noEmailProvided, Toast.LENGTH_LONG).show();
					noEmailsProvidedInfoShown = true;
				}
			}
			i++;
		}

		if(!isAnyEmailAddedToArray){
			emails = new String[1];
			emails[0] = context.getString(R.string.email_utils_error_mockEmailAddress);
		}

		return emails;
	}

	private String buildEmailMessage(Context context, CalculationLogic calc, String endOfLine) {
		StringBuilder sb = new StringBuilder();
		sb.append(context.getString(R.string.email_content_welcome)).append(endOfLine).append(endOfLine);
		sb.append(context.getString(R.string.email_content_text)).append(endOfLine).append(endOfLine);
		sb.append(context.getString(R.string.email_content_madeBy)).append(" ");
		sb.append(getAppUrl(context)).append("!").append(endOfLine).append(endOfLine);
		sb.append(CalculationPrinter.printCalcResultForEmail(
				calc.getCalculationResult(),
				context.getString(R.string.email_calcResult_printText_title),
				context.getString(R.string.email_calcResult_printText_paid),
				context.getString(R.string.email_calcResult_printText_shouldPay),
				context.getString(R.string.email_calcResult_printText_shouldReturn),
				context.getString(R.string.email_calcResult_printText_for),
				endOfLine
				));

		return sb.toString();
	}

	private String getAppUrl(Context context) {
		StringBuilder sb = new StringBuilder();
		sb.append("<a href=\"");
		sb.append(context.getApplicationContext().getString(R.string.app_url));
		sb.append("\">");
		sb.append(context.getString(R.string.app_name_potluck));
		sb.append("</a>");
		return sb.toString();
	}

	public ArrayList<PersonData> readCalcPeopleToListArray(CalculationLogic calc){
		ArrayList<PersonData> listArray = new ArrayList<PersonData>();

		HashMap<String, PersonData> calcResult = calc.getCalculationResult();
		if(calcResult.size() != 0){
			Iterator<String> it = calcResult.keySet().iterator();
			while (it.hasNext()){
				listArray.add(calcResult.get(it.next()));
			}
		}else if(calc.getInputPaysList().size() != 0){
			for (PersonData pd : calc.getInputPaysList()) {
				listArray.add(pd);
			}
		}
		Collections.sort(listArray);
		return listArray;
	}

	public Intent prepareEmailIntent(Context context,
			CalculationLogic calc) {
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.putExtra(Intent.EXTRA_EMAIL, getEmailsArray(context, calc));		  
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.email_subject));
		emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(buildEmailMessage(context, calc, "<br/>")));
		emailIntent.setType("text/html");

		return emailIntent;
	}

}
