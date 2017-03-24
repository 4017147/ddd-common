//   Copyright 2012,2013 Vaughn Vernon
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package cn.mljia.ddd.common.port.adapter.persistence.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate4.HibernateTemplate;

import cn.mljia.ddd.common.AssertionConcern;
import cn.mljia.ddd.common.spring.ApplicationContextProvider;

public class HibernateSupperRepository extends AssertionConcern
{
    
    
    public HibernateTemplate hibernateTemplate(){
       return ApplicationContextProvider.instance().applicationContext().getBean(HibernateTemplate.class);   
    }
    
    public SessionFactory sessionFactory()
    {
        return this.hibernateTemplate().getSessionFactory();
    }
    
    public Session session()
    {
        return this.sessionFactory().getCurrentSession();
    }
    
}
