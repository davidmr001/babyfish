/*
 * BabyFish, Object Model Framework for Java and JPA.
 * https://github.com/babyfish-ct/babyfish
 *
 * Copyright (c) 2008-2016, Tao Chen
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * Please visit "http://opensource.org/licenses/LGPL-3.0" to know more.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 */
package org.babyfishdemo.jpacriteria.base;

import javax.persistence.EntityManagerFactory;

import org.babyfish.hibernate.jpa.HibernatePersistenceProvider;
import org.babyfish.persistence.XEntityManagerFactory;
import org.babyfish.persistence.criteria.QueryTemplate;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author Tao Chen
 */
public abstract class AbstractTest {
    
    protected static XEntityManagerFactory entityManagerFactory;

    @BeforeClass
    public static void initEntityFactory() {
        entityManagerFactory = 
                new HibernatePersistenceProvider()
                .createEntityManagerFactory(null, null);
    }
    
    @AfterClass
    public static void closeEntityFactory() {
        EntityManagerFactory emf = entityManagerFactory;
        if (emf != null) {
            entityManagerFactory = null;
            emf.close();
        }
    }
    
    /*
     * Notes!
     * 
     *      In this demo-project, I use "org.babyfish.persistence.criteria.QueryTemplate"
     * to show you what JPQL has been generated by babyfish-jpa-criteria.
     * 
     *      In real projects, please don't use it unless you want cache the JPQL generation 
     * result in order to do tiny performance optimization. Generically, you'd better use the
     * JPA standard API "EntityManager.createQuery(CriteriaQuery<T>)" and 
     * "org.babyfish.persistence.criteria.QueryTemplate" should be consider as internal interface
     */
    protected static <T> QueryTemplate<T> createQueryTemplate(XCriteriaQuery<T> cq) {
        return entityManagerFactory.createQueryTemplate(cq);
    }
}