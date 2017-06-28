package cn.mljia.ddd.common.port.adapter.persistence.springJdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import cn.mljia.ddd.common.persistence.BeanProcessor;
import cn.mljia.ddd.common.spring.ApplicationContextProvider;

@Component
public class SupportJdbcTemplate
{
    
    private JdbcTemplate jdbcTemplate;
    
    public <T> T queryForBean(String sql, final Class<T> beanType)
    {
        return this.jdbcTemplate().query(sql, new ResultSetExtractor<T>()
        {
            @Override
            public T extractData(ResultSet rs)
                throws SQLException, DataAccessException
            {
                return rs.next() ? new BeanProcessor().toBean(rs, beanType) : null;
            }
        });
    }
    
    public <T> T queryForBean(String sql, final Class<T> beanType, Object... args)
    {
        return this.jdbcTemplate().query(sql, args, new ResultSetExtractor<T>()
        {
            @Override
            public T extractData(ResultSet rs)
                throws SQLException, DataAccessException
            {
                return rs.next() ? new BeanProcessor().toBean(rs, beanType) : null;
            }
        });
    }
    
    public <T> T queryForBean(String sql, final Class<T> beanType, Object[] args, int[] argTypes)
    {
        return this.jdbcTemplate().query(sql, args, argTypes, new ResultSetExtractor<T>()
        {
            @Override
            public T extractData(ResultSet rs)
                throws SQLException, DataAccessException
            {
                return rs.next() ? new BeanProcessor().toBean(rs, beanType) : null;
            }
        });
    }
    
    public <T> List<T> queryForBeanList(String sql, final Class<T> beanType)
    {
        return this.jdbcTemplate().query(sql, new RowMapper<T>()
        {
            @Override
            public T mapRow(ResultSet rs, int rowNum)
                throws SQLException
            {
                return new BeanProcessor().toBean(rs, beanType);
            }
        });
    }
    
    public <T> List<T> queryForBeanList(String sql, final Class<T> beanType, Object... args)
    {
        return this.jdbcTemplate().query(sql, args, new RowMapper<T>()
        {
            @Override
            public T mapRow(ResultSet rs, int rowNum)
                throws SQLException
            {
                return new BeanProcessor().toBean(rs, beanType);
            }
        });
    }
    
    public <T> List<T> queryForBeanList(String sql, final Class<T> beanType, Object[] args, int[] argTypes)
    {
        return this.jdbcTemplate().query(sql, args, argTypes, new RowMapper<T>()
        {
            @Override
            public T mapRow(ResultSet rs, int rowNum)
                throws SQLException
            {
                return new BeanProcessor().toBean(rs, beanType);
            }
        });
    }
    
    public JdbcTemplate jdbcTemplate()
    {
        this.jdbcTemplate = ApplicationContextProvider.instance().applicationContext().getBean(JdbcTemplate.class);
        Assert.notNull(this.jdbcTemplate);
        return this.jdbcTemplate;
    }
}
