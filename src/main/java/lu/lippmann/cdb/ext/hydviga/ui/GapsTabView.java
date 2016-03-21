/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.ext.hydviga.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import lu.lippmann.cdb.command.CommandDispatcher;
import lu.lippmann.cdb.common.FormatterUtil;
import lu.lippmann.cdb.common.async.simplified.AbstractSimpleAsync;
import lu.lippmann.cdb.context.*;
import lu.lippmann.cdb.datasetview.DatasetView;
import lu.lippmann.cdb.datasetview.tabs.AbstractTabView;
import lu.lippmann.cdb.event.EventPublisher;
import lu.lippmann.cdb.ext.hydviga.cbr.*;
import lu.lippmann.cdb.ext.hydviga.data.StationsDataProvider;
import lu.lippmann.cdb.ext.hydviga.gaps.GapFillerFactory.Algo;
import lu.lippmann.cdb.ext.hydviga.util.GapsUtil;
import lu.lippmann.cdb.weka.*;

import org.jdesktop.swingx.*;
import weka.core.*;


/**
 * GapsTabView.
 *
 * @author the WP1 team
 */
public class GapsTabView extends AbstractTabView
{
	//
	// Static fields
	//
	
	/** */
	private static final int MAX_SIZE=1000000;
	/** */
	private static final int PARTS_COUNT=24;
	
	
	//
	// Instance fields
	//
	
	/** */
	private final StationsDataProvider gcp;
	
	/** */
	private final JXPanel jxp;
	
	/** */
	private final JTabbedPane tabPannel;	

	/** */
	private final GapsOverviewPanel gapsPanel;
	
	/** */
	private JXComboBox dateAttributeField;
	
	/** */
	private JXButton fictiveGapButton;
	/** */
	private JXButton showKnowledgeDBButton;
	/** */
	private JXButton inspectKnowledgeDBButton;	
	/** */
	private JXButton showKnowledgeDBWithTrueCasesButton;
	/** */
	private JXButton rebuildKnowledgeDBButton;

	/** */
	private JXPanel actionsForFictiveGapsPanel;
	
	private final EventPublisher eventPublisher;
	private final CommandDispatcher commandDispatcher;
	private final ApplicationContext applicationContext;
	
	
	//
	// Constructor
	//
	
	/**
	 * Constructor.
	 */
	public GapsTabView(final StationsDataProvider gcp,final EventPublisher eventPublisher,final CommandDispatcher commandDispatcher,final ApplicationContext applicationContext)
	{	
		super();
		
		this.gcp=gcp;
		
		this.eventPublisher=eventPublisher;
		this.commandDispatcher=commandDispatcher;
		this.applicationContext=applicationContext;
		
		this.jxp=new JXPanel();		
		this.jxp.setLayout(new BorderLayout());		
				
		this.tabPannel=new JTabbedPane();
		
		this.gapsPanel=new GapsOverviewPanel(this,gcp);
		this.tabPannel.addTab("Gaps",gapsPanel.getComponent());
		
		this.jxp.add(this.tabPannel,BorderLayout.CENTER);
					
		//final JXPanel actionsPanel=new JXPanel();
		this.actionsForFictiveGapsPanel=new JXPanel();
		//actionsPanel.add(this.listOfPossibleSeriesForFictiveGapsPanel);
		
		this.jxp.add(this.actionsForFictiveGapsPanel,BorderLayout.NORTH);
	}

	
	//
	// Instance methods
	//
	
	private AbstractTabView getAbstractTabView()
	{
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSlow()
	{
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() 
	{
		return "Time series";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean needsClassAttribute()
	{
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean needsDateAttribute()
	{
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getComponent() 
	{					
		return this.jxp;
	}
	
	private void fillTabs(final Instances dataSet)
	{			
		if (dataSet.numInstances()>MAX_SIZE)
		{
			throw new IllegalStateException("Time series are too long ("+dataSet.numInstances()+"), and records count should be > "+MAX_SIZE+": please filter data before using it.");
		}
		
		final int dateIdx=dataSet.attribute(dateAttributeField.getSelectedItem().toString()).index();		
			
		System.out.println("TimeSeriesTabView: building 'gaps' subpanel ...");
		gapsPanel.refresh(dataSet,dateIdx);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update0(final Instances dataSet) throws Exception 
	{		 					
		if (this.dateAttributeField!=null) 
		{	
			this.jxp.remove(this.dateAttributeField);
			this.dateAttributeField=null;
			this.jxp.updateUI();
		}
		
		final java.util.List<String> dateAttributeNames=WekaDataStatsUtil.getDateAttributeNames(dataSet);
		final boolean hasDateAttributes=(!dateAttributeNames.isEmpty())
				/*&&(WekaDataStatsUtil.getNumericAttributesIndexes(dataSet).size()>0)*/;		
		
		if (hasDateAttributes) 
		{						
			this.dateAttributeField=new JXComboBox(dateAttributeNames.toArray());	
			this.dateAttributeField.setBorder(new TitledBorder("Date attribute"));
			this.jxp.add(this.dateAttributeField,BorderLayout.SOUTH);
			this.dateAttributeField.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					fillTabs(dataSet);				
				}
			});

			new AbstractSimpleAsync<Void>(true)
			{
				@Override
				public Void execute() throws Exception 
				{
					fillTabs(dataSet);
					return null;
				}

				@Override
				public void onSuccess(Void result) {}

				@Override
				public void onFailure(Throwable caught) 
				{
					caught.printStackTrace();					
				}
			}.start();

			this.actionsForFictiveGapsPanel.removeAll();									
			//final JComboBox seriesWithoutGapCB=new JComboBox(WekaTimeSeriesUtil.getNamesOfAttributesWithoutGap(dataSet).toArray());
			final JComboBox seriesWithoutGapCB=new JComboBox(WekaDataStatsUtil.getAttributeNames(dataSet).toArray());
			seriesWithoutGapCB.setBorder(new TitledBorder("Fictive gap in"));
			this.actionsForFictiveGapsPanel.add(seriesWithoutGapCB);						
			final JComboBox sizeGapCB=new JComboBox(new Object[]{10,50,100,200,400,500});
			sizeGapCB.setBorder(new TitledBorder("Size of the fictive gap"));
			this.actionsForFictiveGapsPanel.add(sizeGapCB);
			final Object[] partChoice=new Object[PARTS_COUNT];
			for (int iii=0;iii<PARTS_COUNT;iii++)
			{
				partChoice[iii]=iii+"/"+PARTS_COUNT;
			}
			
			final JComboBox positionGapCB=new JComboBox(partChoice);
			positionGapCB.setBorder(new TitledBorder("Position of the fictive gap"));
			this.actionsForFictiveGapsPanel.add(positionGapCB);
			
			this.fictiveGapButton=new JXButton("Create a fictive gap");
			this.actionsForFictiveGapsPanel.add(this.fictiveGapButton);
			
			this.fictiveGapButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					/* create a fake gap */
					final Attribute attr=dataSet.attribute(seriesWithoutGapCB.getSelectedItem().toString());
					final int dateIdx=WekaDataStatsUtil.getFirstDateAttributeIdx(dataSet);
					final int position=((positionGapCB.getSelectedIndex()+1)*dataSet.numInstances())/PARTS_COUNT;
					final int gapsize=Integer.valueOf(sizeGapCB.getSelectedItem().toString());
					
					/* show it */
					final GapFillingFrame jxf=new GapFillingFrame(getAbstractTabView(),new Instances(dataSet),attr,dateIdx,GapsUtil.getCountOfValuesBeforeAndAfter(gapsize),position,gapsize,gcp,false);
					//jxf.setSize(new Dimension(900,700));
					//jxf.setExtendedState(Frame.MAXIMIZED_BOTH);								
					jxf.setLocationRelativeTo(jxp);
					jxf.setVisible(true);						
					//jxf.setResizable(false);				
				}
			});
			
			this.showKnowledgeDBButton=new JXButton("Show KDB");
			this.actionsForFictiveGapsPanel.add(this.showKnowledgeDBButton);			
			this.showKnowledgeDBButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e) 
				{
					
					try 
					{
						final DatasetView view=new DatasetView("KnowledgeDB",eventPublisher,commandDispatcher,applicationContext);
						view.setDataSet(GapFillingKnowledgeDB.getKnowledgeDB()).setAsVisible(true);
					} 
					catch (final Exception ee) 
					{			
						ee.printStackTrace();
					}					
				}
			});
			
			this.inspectKnowledgeDBButton=new JXButton("Inspect KDB");
			this.actionsForFictiveGapsPanel.add(this.inspectKnowledgeDBButton);			
			this.inspectKnowledgeDBButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e) 
				{					
					try 
					{
						final int dateIdx=WekaDataStatsUtil.getFirstDateAttributeIdx(dataSet);
						new GapFillingKnowledgeDBExplorerFrame(dataSet,dateIdx,gcp);
					} 
					catch (final Exception ee) 
					{			
						ee.printStackTrace();
					}					
				}
			});
			
			this.showKnowledgeDBWithTrueCasesButton=new JXButton("Show KDB with true cases");
			this.actionsForFictiveGapsPanel.add(this.showKnowledgeDBWithTrueCasesButton);			
			this.showKnowledgeDBWithTrueCasesButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e) 
				{					
					try 
					{						
						final StringBuilder sb=new StringBuilder("@relation blabla\n");
						sb.append("@attribute serieName string\n");
						sb.append("@attribute serieX numeric\n");
						sb.append("@attribute serieY numeric\n");
						sb.append("@attribute year numeric\n");
						sb.append("@attribute season {Winter,Spring,Summer,Autumn}\n");
						sb.append("@attribute gapSize numeric\n");
						sb.append("@attribute gapPosition numeric\n");
						sb.append("@attribute isDuringRising {true,false}\n");
						sb.append("@attribute flow string\n");
						sb.append("@attribute hasDownstream {false,true}\n");
						sb.append("@attribute hasUpstream {false,true}\n");
						sb.append("@attribute isReal {false,true}\n");						
						sb.append("@attribute algo {Interpolation,EM,REG,REPTREE,M5P,ZeroR,ANN,NEARESTNEIGHBOUR}\n");
						sb.append("@attribute useDiscretizedTime {false,true}\n");
						sb.append("@attribute useMostSimilar {false,true}\n");
						sb.append("@attribute useNearest {true,false}\n");
						sb.append("@attribute useDownstream {false,true}\n");
						sb.append("@attribute useUpstream {true,false}\n");
						sb.append("@attribute mae numeric\n");
						sb.append("@attribute rmse numeric\n");
						sb.append("@attribute rsr numeric\n");
						sb.append("@attribute pbias numeric\n");						
						sb.append("@attribute ns numeric\n");
						sb.append("@attribute ioa numeric\n");
						sb.append("@attribute wasTheBestSolution {true,false}\n");
						
						sb.append("@data\n");
						
						/* true cases */
						final Calendar cal=Calendar.getInstance();
						final int dateIdx=WekaDataStatsUtil.getFirstDateAttributeIdx(dataSet);
						final Instances gapsDescriptionsDataset=GapsUtil.buildGapsDescription(gcp,dataSet,dateIdx);
						final int gddc=gapsDescriptionsDataset.numInstances();
						for (int i=0;i<gddc;i++)
						{													
							final Instance trueCase=gapsDescriptionsDataset.instance(i);
							sb.append(trueCase.stringValue(0)); // serie
							sb.append(",");
							sb.append(gcp.getCoordinates(trueCase.stringValue(0))[0]); // x
							sb.append(",");
							sb.append(gcp.getCoordinates(trueCase.stringValue(0))[1]); // y
							sb.append(",");
							cal.setTime(FormatterUtil.DATE_FORMAT.parse(trueCase.stringValue(1))); // year
							sb.append(cal.get(Calendar.YEAR));
							sb.append(",");
							sb.append(trueCase.stringValue(2).split("/")[0]); // season
							sb.append(",");
							sb.append(trueCase.value(4)); //gapsize
							sb.append(",");
							sb.append(trueCase.value(5)); //gap position
							sb.append(",");
							sb.append(trueCase.stringValue(10).equals("true")); //rising
							sb.append(",");
							sb.append(trueCase.stringValue(11)); // flow
							sb.append(",");
							sb.append(!trueCase.stringValue(9).equals("n/a")); //downstream
							sb.append(",");
							sb.append(!trueCase.stringValue(8).equals("n/a")); // upstream
							sb.append(",");
							sb.append("true");
							sb.append(",");
							sb.append("?");
							sb.append(",");
							sb.append("?");
							sb.append(",");
							sb.append("?");
							sb.append(",");
							sb.append("?");
							sb.append(",");
							sb.append("?");
							sb.append(",");
							sb.append("?");
							sb.append(",");
							sb.append("?");
							sb.append(",");
							sb.append("?");
							sb.append(",");
							sb.append("?");
							sb.append(",");
							sb.append("?");
							sb.append(",");
							sb.append("?");
							sb.append(",");														
							sb.append("?");
							sb.append(",");														
							sb.append("?");	
							sb.append("\n");						
						}						
						
						/* the simulated cases from the knowledge DB */
						final Instances knowledgeDB=GapFillingKnowledgeDB.getKnowledgeDB();
						final int kni=knowledgeDB.numInstances();
						for (int i=0;i<kni;i++)
						{													
							final Instance simulatedCase=knowledgeDB.instance(i);
							sb.append(simulatedCase.stringValue(0)); // name
							sb.append(",");
							sb.append(simulatedCase.value(1)); //x
							sb.append(",");
							sb.append(simulatedCase.value(2));		//y					
							sb.append(",");
							sb.append(simulatedCase.value(6)); //year
							sb.append(",");
							sb.append(simulatedCase.stringValue(5)); //season
							sb.append(",");
							sb.append(simulatedCase.value(3)); // size
							sb.append(",");
							sb.append(simulatedCase.value(4)); // position
							sb.append(",");
							sb.append(simulatedCase.stringValue(7)); // rising
							sb.append(",");
							sb.append(simulatedCase.stringValue(8)); //flow
							sb.append(",");
							sb.append(simulatedCase.stringValue(9)); //downstream
							sb.append(",");
							sb.append(simulatedCase.stringValue(10)); // upstream
							sb.append(",");
							sb.append("false"); // real
							sb.append(",");
							sb.append(simulatedCase.stringValue(11)); //algo
							sb.append(",");
							sb.append(simulatedCase.stringValue(12)); // discr time
							sb.append(",");
							sb.append(simulatedCase.stringValue(13)); // most similar
							sb.append(",");
							sb.append(simulatedCase.stringValue(14)); // nearest
							sb.append(",");
							sb.append(simulatedCase.stringValue(15)); //downstream
							sb.append(",");
							sb.append(simulatedCase.stringValue(16)); //upstream
							sb.append(",");
							sb.append(simulatedCase.value(17)); //mae
							sb.append(",");
							sb.append(simulatedCase.value(18)); //rmse
							sb.append(",");
							sb.append(simulatedCase.value(19)); //rsr
							sb.append(",");
							sb.append(simulatedCase.value(20)); //pbias
							sb.append(",");
							sb.append(simulatedCase.value(21)); //ns
							sb.append(",");
							sb.append(simulatedCase.value(22)); //ioa
							sb.append(",");	
							sb.append(simulatedCase.stringValue(23));		// best												
							sb.append("\n");						
						}
						
						//System.out.println(sb.toString());
						
						final Instances newds=WekaDataAccessUtil.loadInstancesFromARFFString(sb.toString(),false,false);
						final DatasetView view=new DatasetView("KnowledgeDB with true cases",eventPublisher,commandDispatcher,applicationContext);
						view.setDataSet(newds).setAsVisible(true);											
					} 
					catch (final Exception ee) 
					{			
						ee.printStackTrace();
					}					
				}
			});
			
			this.rebuildKnowledgeDBButton=new JXButton("Rebuild KDB");
			this.actionsForFictiveGapsPanel.add(this.rebuildKnowledgeDBButton);			
			this.rebuildKnowledgeDBButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e) 
				{
					rebuildKnowledgeDB(dataSet);			
				}
			});
		}
		else 
		{	
			throw new Exception("No date attributes in the dataset.");
		}				
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Icon getIcon() 
	{
		return ResourceLoader.getAndCacheIcon("menu/time-series.png");
	}


	private void rebuildKnowledgeDB(final Instances dataSet) 
	{
		GapFillingKnowledgeDB.clear();
		
		final int dateIdx=WekaDataStatsUtil.getFirstDateAttributeIdx(dataSet);
		
		//for (final String serie:WekaTimeSeriesUtil.getNamesOfAttributesWithoutGap(dataSet))
		for (final String serie:WekaDataStatsUtil.getAttributeNames(dataSet))
		{
			if (serie.equals("timestamp")) continue;
			
			for (int part=1;part<PARTS_COUNT;part++)
			{
				//for (final int gapsize:new int[]{10})
				//for (final int gapsize:new int[]{2,3,4,5,6,7,8,10,20,50,75,100})
				//*for (final int gapsize:new int[]{2,3,4,5,6,7,8})
				//for (final int gapsize:new int[]{10,20,50,75,100})
				for (final int gapsize:new int[]{40})
				{
					System.out.println(serie+": part="+part+"/"+PARTS_COUNT+" gapsize="+gapsize);
					
					/* create a fake gap */
					final Attribute attr=dataSet.attribute(serie);
					final int position=(part*dataSet.numInstances())/PARTS_COUNT;

					/* process it for each algo and for different combination */
					final GapFillingFrame jxf=new GapFillingFrame(getAbstractTabView(),new Instances(dataSet),attr,dateIdx,GapsUtil.getCountOfValuesBeforeAndAfter(gapsize),position,gapsize,gcp,true);
					
					if (!jxf.isGapSimulated())
					{
						System.out.println("Gap already present here - skip it");
						continue;
					}
					
					final java.util.List<GapFillingCase> l=new ArrayList<GapFillingCase>();
					for (final Algo algo:Algo.values())
					{
						System.out.println("\t"+algo);
						
						try 
						{					
							final int idxNearest=jxf.getUsedAttributes().indexOf(jxf.getNearest());
							l.add(jxf.refresh(algo,new int[]{idxNearest}));
							
							final int idxMostSimilar=jxf.getUsedAttributes().indexOf(jxf.getMostSimilar());
							l.add(jxf.refresh(algo,new int[]{idxMostSimilar}));
							
							l.add(jxf.refresh(algo,new int[]{idxNearest,idxMostSimilar}));
							
							final String up=jxf.getUpstream();
							final int idxUpstream=(up!=null)?jxf.getUsedAttributes().indexOf(up):-1;
							if (up!=null)
							{
								l.add(jxf.refresh(algo,new int[]{idxUpstream}));
								l.add(jxf.refresh(algo,new int[]{idxNearest,idxUpstream}));
								l.add(jxf.refresh(algo,new int[]{idxMostSimilar,idxUpstream}));
								l.add(jxf.refresh(algo,new int[]{idxNearest,idxMostSimilar,idxUpstream}));
							}
							
							final String down=jxf.getDownstream();
							final int idxDownstream=(down!=null)?jxf.getUsedAttributes().indexOf(down):-1;
							if (down!=null)
							{
								l.add(jxf.refresh(algo,new int[]{idxDownstream}));
								l.add(jxf.refresh(algo,new int[]{idxNearest,idxDownstream}));
								l.add(jxf.refresh(algo,new int[]{idxMostSimilar,idxDownstream}));
								l.add(jxf.refresh(algo,new int[]{idxNearest,idxMostSimilar,idxDownstream}));
							}
							
							if (up!=null&&down!=null)
							{
								l.add(jxf.refresh(algo,new int[]{idxNearest,idxUpstream,idxDownstream}));
								l.add(jxf.refresh(algo,new int[]{idxMostSimilar,idxUpstream,idxDownstream}));
								l.add(jxf.refresh(algo,new int[]{idxNearest,idxMostSimilar,idxUpstream,idxDownstream}));
								l.add(jxf.refresh(algo,new int[]{idxUpstream,idxDownstream}));
							}
						} 
						catch (Exception e1) 
						{										
							e1.printStackTrace();
						}
					}
					
					/* mark the best solution */
					double minRMSE=Double.POSITIVE_INFINITY;
					int minIDX=-1;
					for (int i=0;i<l.size();i++)
					{
						final GapFillingCase curr=l.get(i);
						if (curr.getRMSE()<minRMSE)
						{
							minRMSE=curr.getRMSE();
							minIDX=i;
						}
					}
					if (minIDX>=0) l.get(minIDX).setWasTheBestSolution(true);
					else throw new IllegalStateException("no best solution?");
					GapFillingKnowledgeDB.storeCasesIntoKnowledgeDB(l);
				}
			}
			//break;
		}
		System.out.println("finished");
				

	}
}
