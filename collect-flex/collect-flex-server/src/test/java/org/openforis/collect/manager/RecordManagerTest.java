package org.openforis.collect.manager;

import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

//@RunWith( SpringJUnit4ClassRunner.class )
//@ContextConfiguration( locations = {"classpath:test-context.xml"} )
//@TransactionConfiguration(transactionManager="transactionManager", defaultRollback=false)
//@Transactional
public class RecordManagerTest {
	
    private static Logger logger = Logger.getLogger(RecordManagerTest.class.getName());
    
//    @PersistenceContext
//    private EntityManager em;
    
//    @BeforeClass
    public static void setUp() throws Exception {
//        try {
//            logger.info("Starting in-memory database for unit tests");
//            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
//            DriverManager.getConnection("jdbc:derby:memory:unit-testing-jpa;create=true").close();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            Assert.fail("Exception during database startup.");
//        }
//        try {
//            logger.info("Building JPA EntityManager for unit tests");
//            emFactory = Persistence.createEntityManagerFactory("testPU");
//            em = emFactory.createEntityManager();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            Assert.fail("Exception during JPA EntityManager instanciation.");
//        }
    }


//    @AfterClass
    public static void tearDown() throws Exception {
//        logger.info("Shutting down Hibernate JPA layer.");
//        if (em != null) {
//            em.close();
//        }
//        if (emFactory != null) {
//            emFactory.close();
//        }
//        logger.info("Stopping in-memory database.");
//        try {
//            DriverManager.getConnection("jdbc:derby:memory:unit-testing-jpa;shutdown=true").close();
//        } catch (SQLNonTransientConnectionException ex) {
//            if (ex.getErrorCode() != 45000) {
//                throw ex;
//            }
//            // Shutdown success
//        }
//        VFMemoryStorageFactory.purgeDatabase(new File("unit-testing-jpa").getCanonicalPath());
    }
    
//    @Test
    public void testPersistence() {
        try {

//            em.getTransaction().begin();

//            em.persist(val);
//            Assert.assertTrue(em.contains(val));
            
//            g.removeUser(u);
//            em.remove(u);d
//            em.merge(g);
//            assertFalse(em.contains(u));
//
//            em.getTransaction().commit();

        } catch (Exception ex) {
//            em.getTransaction().rollback();
            ex.printStackTrace();
            Assert.fail("Exception during testPersistence");
        }
    }
}
