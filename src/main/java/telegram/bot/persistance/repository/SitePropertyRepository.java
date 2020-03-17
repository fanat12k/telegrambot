package telegram.bot.persistance.repository;

import io.micronaut.spring.tx.annotation.Transactional;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import telegram.bot.persistance.domain.SiteProperty;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import static telegram.bot.persistance.Fields.KEY;
import static telegram.bot.persistance.Fields.VALUE;
@Singleton
public class SitePropertyRepository {
  private static final String[] FIELDS = new String[]{KEY, VALUE};
  private static final String TABLE_NAME = "site.site_properties";
  private JdbcTemplate jdbcTemplate;
  private SimpleJdbcInsert entityJdbcInsert;
  private DSLContext dslContext;

  public SitePropertyRepository(JdbcTemplate jdbcTemplate, DSLContext dslContext) {

   this.jdbcTemplate = jdbcTemplate;

    entityJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName(TABLE_NAME).usingColumns(FIELDS);
    this.dslContext = dslContext;
  }

  public void save(SiteProperty property) {

    entityJdbcInsert.compile();

    MapSqlParameterSource params = new MapSqlParameterSource();

    params.addValue(KEY, property.getKey());
    params.addValue(VALUE, property.getValue());

    entityJdbcInsert.executeBatch(params);
  }

  @Transactional
  public boolean update(SiteProperty entity) {
    Query query = dslContext.update(table(TABLE_NAME)).set(field(VALUE), entity.getValue()).where(field(KEY).eq(entity.getKey()));

    return jdbcTemplate.update(query.getSQL(), query.getBindValues().toArray()) != 0;
  }


  public Optional<SiteProperty> get(String key) {
    Query query = dslContext.select(Stream.of(FIELDS).map(DSL::field).toArray(Field[]::new)).from(table(TABLE_NAME)).where(field(KEY).eq(key));

    try {
      return Optional.ofNullable(jdbcTemplate.queryForObject(query.getSQL(), (rs, rowNum) -> {
        SiteProperty entity = new SiteProperty();

        entity.setKey(rs.getString(KEY));
        entity.setValue(rs.getString(VALUE));

        return entity;
      }, query.getBindValues().toArray()));
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }
}
