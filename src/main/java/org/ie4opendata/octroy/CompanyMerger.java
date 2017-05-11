package org.ie4opendata.octroy;

// licensed under APL-2.0

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.uimafit.util.JCasUtil;

public class CompanyMerger extends JCasAnnotator_ImplBase {

	public void process(JCas aJCas) {

		// find companies with NEQ and put their starts and end in tables
		Map<Integer, List<Company>> neqStarts = new HashMap<>();
		Map<Integer, List<Company>> neqEnds = new HashMap<>();
		List<Company> toDelete = new ArrayList<>();
		List<Company> mlCompanies = new ArrayList<>();

		for (Company company : JCasUtil.select(aJCas, Company.class))
			if (company.getRegistrationNumber() != null) {
				int begin = company.getBegin();
				int end = company.getEnd();
				if (!neqStarts.containsKey(begin))
					neqStarts.put(begin, new ArrayList<Company>());
				if (!neqEnds.containsKey(end))
					neqEnds.put(end, new ArrayList<Company>());
				neqStarts.get(begin).add(company);
				neqEnds.get(end).add(company);
				toDelete.add(company);
			} else
				mlCompanies.add(company);
		for (Company company : mlCompanies) {
			int begin = company.getBegin();
			int end = company.getEnd();
			Company other = null;
			if (neqStarts.containsKey(begin))
				for (Company startsThere : neqStarts.get(begin))
					if (other == null || other.getCoveredText().length() < startsThere.getCoveredText().length())
						other = startsThere;
			if (neqEnds.containsKey(end))
				for (Company endsThere : neqStarts.get(end))
					if (other == null || other.getCoveredText().length() < endsThere.getCoveredText().length())
						other = endsThere;
			if (other != null) {
				company.setRegistrationNumber(other.getRegistrationNumber());
				company.setOfficialName(other.getOfficialName());
			}
		}
		for (Company company : toDelete)
			company.removeFromIndexes();

	}
}
