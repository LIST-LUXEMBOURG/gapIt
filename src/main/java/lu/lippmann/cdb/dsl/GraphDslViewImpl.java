/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.dsl;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.text.*;

import lu.lippmann.cdb.command.*;
import lu.lippmann.cdb.common.gui.LogoHelper;
import lu.lippmann.cdb.common.guice.util.AutoBind;
import lu.lippmann.cdb.common.mvp.Listener;
import lu.lippmann.cdb.models.CVariable;

import org.jdesktop.swingx.*;
import com.google.inject.Inject;


/**
 * Graph DSL view implementation.
 * 
 * @author Olivier PARISOT
 */
@AutoBind
public class GraphDslViewImpl extends JXFrame implements GraphDslView 
{
	//
	// Static fields
	//

	/** Serial version UID. */
	private static final long serialVersionUID=159207491L;


	//
	// Instance fields
	//

	/** */
	private final JTextPane infoTxtPane;	
	/** */
	private final JTextPane dsltxtPane;
	/** */
	private final JTextPane errorstxtPane;

	/** */
	private Listener<String> listener;

	/** */
	private String dslString;

	/** */
	private List<Pattern> dslKeywordPatterns;

	/** */
	private Map<String,Pattern> variableNamePatterns;

	/** */
	private Pattern commentPattern;

	/** */
	@Inject
	private CommandDispatcher commandDispatcher;
	
	//
	// Constructors.
	//

	/**
	 * Constructor.
	 */
	public GraphDslViewImpl() 
	{
		setTitle("DSL viewer");

		LogoHelper.setLogo(this);

		getContentPane().removeAll();
		getContentPane().setLayout(new BorderLayout());

		final JXPanel mainPanel=new JXPanel();		
		mainPanel.setLayout(new BorderLayout());

		this.infoTxtPane=new JTextPane();
		this.infoTxtPane.setBackground(Color.LIGHT_GRAY);
		this.infoTxtPane.setEditable(false);
		mainPanel.add(infoTxtPane,BorderLayout.NORTH);

		this.dsltxtPane=new JTextPane();		
		mainPanel.add(this.dsltxtPane,BorderLayout.CENTER);

		this.errorstxtPane=new JTextPane();
		this.errorstxtPane.setEditable(false);
		this.errorstxtPane.setForeground(Color.RED);
		mainPanel.add(this.errorstxtPane,BorderLayout.EAST);		
		
		final JXButton jxb=new JXButton("Ok");
		jxb.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				highlight();
				final String newDslStringFormat=dsltxtPane.getText();
				if (newDslStringFormat.compareTo(dslString)!=0)
				{
					dslString=newDslStringFormat;
					listener.onAction(newDslStringFormat);
				}				
			}
		});
		mainPanel.add(jxb,BorderLayout.SOUTH);

		getContentPane().add(mainPanel,BorderLayout.CENTER);		
		
		//Call exit listener when clicking window exit button
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); 
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				commandDispatcher.dispatch(new ChangeDSLVisibleCommand());
			};
		});

	}


	//
	// Instance methods
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reInit() 
	{	
		dsltxtPane.setText(dslString);
		highlight();

		setPreferredSize(new Dimension(400,400));
		pack();
		setVisible(true);
	}

	/**
	 * Process syntaxic coloration.
	 */
	private void highlight() 
	{
		clearHighlight(dsltxtPane);
		highlightKeywords();
		highlightVariables();
		highlightComments();
	}

	/**
	 * 
	 */
	private void highlightKeywords() 
	{
		highlight(dsltxtPane,dslKeywordPatterns,Color.BLUE,true,false);
	}


	/**
	 * 
	 */
	private void highlightVariables() 
	{
		highlight(dsltxtPane,variableNamePatterns.values(),Color.GREEN,true,false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component asComponent() 
	{
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDslString(final String dslString) 
	{
		this.dslString=dslString;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setOnDslStringChangedListener(final Listener<String> listener) 
	{
		this.listener=listener;				
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setDslKeywords(final String[] dslKeywords) 
	{
		this.dslKeywordPatterns=new ArrayList<Pattern>();
		for (String k:dslKeywords)
		{
			this.dslKeywordPatterns.add(Pattern.compile("(^|\\s*)("+k+")(\\s*|$)"));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setVariables(Set<CVariable> variables) 
	{
		this.variableNamePatterns=new HashMap<String,Pattern>();
		for (final CVariable k:variables)
		{
			if (!this.variableNamePatterns.containsKey(k.getKey()))
			{
				this.variableNamePatterns.put(k.getKey(),Pattern.compile("(^|\\s*)("+k.getKey()+")(\\s*|$)"));
			}
		}
		highlightVariables();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCommentMarker(final String commentMarker) 
	{
		this.commentPattern=Pattern.compile("(?m)^"+commentMarker+"(.*|$)");
	}

	private void highlightComments()
	{
		final StyledDocument doc=dsltxtPane.getStyledDocument();
		final Matcher m=commentPattern.matcher(dsltxtPane.getText());
		while (m.find()) 
		{				
			final int start=m.start();
			final int end=m.end();
			final int length=end-start;

			SwingUtilities.invokeLater(new Runnable() 
			{
				public void run() 
				{
					final MutableAttributeSet attri=new SimpleAttributeSet();
					StyleConstants.setForeground(attri,Color.GRAY);
					StyleConstants.setItalic(attri,true);
					doc.setCharacterAttributes(start,length,attri,true);
				}
			});
		}			
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateLinesWithError(final Map<Integer,String> linesWithError) 
	{
		final Set<Integer> remainingLinesWithError=new TreeSet<Integer>();
		remainingLinesWithError.addAll(linesWithError.keySet());

		final StringBuilder sb=new StringBuilder();
		for (int i=0;i<1000;i++)
		{			
			if (remainingLinesWithError.contains(i)&&linesWithError.containsKey(i))
			{
				sb.append(linesWithError.get(i));
				remainingLinesWithError.remove(i);
			}
			sb.append("\n");
			if (remainingLinesWithError.isEmpty()) break;
		}
		//System.out.println(sb.toString());
		this.errorstxtPane.setText(sb.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDslFormat(final String dslFormat) 
	{				
		infoTxtPane.setText("Here is the textual representation of the model, using the following format:\n\n\t"+dslFormat+"\n\n");		
	}	


	//
	// Static methods
	//

	private static void clearHighlight(final JTextPane c)
	{
		final StyledDocument doc = c.getStyledDocument();
		final MutableAttributeSet normal= new SimpleAttributeSet();
		StyleConstants.setForeground(normal, Color.black);
		StyleConstants.setBold(normal, false);
		SwingUtilities.invokeLater(new Runnable() 
		{
			public void run() 
			{
				doc.setCharacterAttributes(0, doc.getLength(), normal, true);
			}
		});
	}

	private static void highlight(final JTextPane c,final Collection<Pattern> patterns,final Color color,final boolean bold,final boolean italic)
	{
		final StyledDocument doc=c.getStyledDocument();
		for (final Pattern p:patterns) 
		{
			final Matcher m = p.matcher(c.getText());
			while (m.find()) 
			{				
				final int start=m.start(2);
				final int end=m.end(2);
				final int length=end-start;

				SwingUtilities.invokeLater(new Runnable() 
				{
					public void run() 
					{
						final MutableAttributeSet attri=new SimpleAttributeSet();
						StyleConstants.setForeground(attri,color);
						StyleConstants.setBold(attri,bold);
						StyleConstants.setItalic(attri,italic);
						doc.setCharacterAttributes(start,length,attri,true);
					}
				});
			}
		}
	}


}
