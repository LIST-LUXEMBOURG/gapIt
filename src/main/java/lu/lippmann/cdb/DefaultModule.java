/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb;

import lu.lippmann.cdb.command.*;
import lu.lippmann.cdb.common.guice.util.SimpleAutoBindingModule;
import lu.lippmann.cdb.common.mvp.Presenter;
import lu.lippmann.cdb.context.*;
import lu.lippmann.cdb.dsl.*;
import lu.lippmann.cdb.event.*;
import lu.lippmann.cdb.graph.GraphPresenter;
import lu.lippmann.cdb.panes.history.HistoryPresenter;
import lu.lippmann.cdb.repositories.*;
import lu.lippmann.cdb.repositories.impl.memory.MemoryCVariablesRepositoryImpl;
import lu.lippmann.cdb.repositories.impl.neo4j.EmbeddedNeo4jCGraphRepositoryImpl;
import lu.lippmann.cdb.services.AuthentificationService;
import lu.lippmann.cdb.services.impl.memory.MemoryAuthentificationServiceImpl;

import com.google.inject.*;


/**
 * Module defining the application.
 * 
 * @author the ACORA team
 */
public class DefaultModule extends AbstractModule 
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configure() 
	{
		bind(ApplicationContext.class).in(Singleton.class);
		bind(CommandDispatcher.class).to(CommandDispatcherBushImpl.class).in(Singleton.class);
		bind(EventPublisher.class).to(EventPublisherBushImpl.class).in(Singleton.class);

		bind(BusLogger.class).in(Singleton.class);
		
		install(new SimpleAutoBindingModule("lu.lippmann"));				
		
		//bind(CGraphRepository.class).to(FileCGraphRepositoryImpl.class).in(Singleton.class);
		//bind(CGraphRepository.class).to(MemoryCGraphRepositoryImpl.class).in(Singleton.class);
		bind(CGraphRepository.class).to(EmbeddedNeo4jCGraphRepositoryImpl.class).in(Singleton.class);
		
		bind(CVariablesRepository.class).to(MemoryCVariablesRepositoryImpl.class).in(Singleton.class);

		//bind(GraphDsl.class).to(SimpleGraphDsl.class).in(Singleton.class);
		bind(GraphDsl.class).to(ASCIIGraphDsl.class).in(Singleton.class);
		
		bind(Presenter.class).to(HistoryPresenter.class);		

		bind(GraphPresenter.class).in(Singleton.class);
		
		bind(AuthentificationService.class).to(MemoryAuthentificationServiceImpl.class);		
		//bind(AuthentificationView.class).to(AutoAuthentificationViewImpl.class);
		//bind(AuthentificationView.class).to(DialogBoxAuthentificationViewImpl.class);

	}

}
