package com.mcafee.mam.auto.infra.log.html;

import java.io.File;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.hp.gagawa.java.elements.Div;

public class HtmlScenarioSummary extends HtmlSummary
{
	public HtmlScenarioSummary(File file)
	{
		super(file);
	}

	public void build(GeneralStatistics generalStatistics, TestStatistics testStatistics)
	{
		initPage();
		createHtmlSummaryHeader();
		this.testOrderList.add(testStatistics);
		Collections.sort(this.testOrderList, new Comparator<TestStatistics>()
		{
			@Override
			public int compare(TestStatistics o1, TestStatistics o2)
			{
				return Collator.getInstance().compare(o1.getScenarioFile(), o2.getScenarioFile());
			}
		});
		Div div2 = buildStatisticsTable();
		Div div1 = createGeneralStatisticsTable(generalStatistics);

		body.appendChild(div1);
		body.appendChild(div2);
		save();
	}

	private Div buildStatisticsTable()
	{
		Div div = new Div();
		this.index = 1;
		List<TestStatistics> tsl = new LinkedList<TestStatistics>();
		String lastScenario = "";
		for (TestStatistics ts : this.testOrderList)
		{
			this.totalRunningTime += ts.getRunningTime();
			if (!lastScenario.isEmpty())
			{
				if (!lastScenario.equals(ts.getScenarioFile()))
				{
					div.appendChild(createHtmlSortHeader(lastScenario));
					div.appendChild(createHtmlTestStatisticsTable(tsl));
					tsl.clear();
				}
			}
			tsl.add(ts);
			lastScenario = ts.getScenarioFile();
		}
		if (!tsl.isEmpty())
		{
			div.appendChild(createHtmlSortHeader(lastScenario));
			div.appendChild(createHtmlTestStatisticsTable(tsl));
		}
		return div;
	}

}
