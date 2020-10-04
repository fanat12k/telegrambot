package telegram.bot.persistance.repository;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import telegram.bot.persistance.domain.TelegramUser;

import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.table;
import static telegram.bot.persistance.Fields.*;

@Singleton
public class TelegramRepository {
  private static final String[] FIELDS = new String[]{ACCESS, USER_ID, ACCESS_BOT_NAME};
  private static final String[] INSERT_FIELDS = new String[]{USER_ID, USER_NAME};
  private static final String TABLE_NAME = "site.telegram_user_list";
  private JdbcTemplate jdbcTemplate;
  private SimpleJdbcInsert entityJdbcInsert;
  private DSLContext dslContext;


  public TelegramRepository(JdbcTemplate jdbcTemplate, DSLContext dslContext) {
    this.jdbcTemplate = jdbcTemplate;
    entityJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName(TABLE_NAME).usingColumns(INSERT_FIELDS);
    this.dslContext = dslContext;
  }

  public void save(TelegramUser property) {
    entityJdbcInsert.compile();
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue(USER_ID, property.getUserId());
    params.addValue(USER_NAME, property.getUserName());
    entityJdbcInsert.executeBatch(params);
  }

  public List<TelegramUser> list(String botName) {

    Query query = dslContext.select(Stream.of(FIELDS).map(DSL::field).toArray(Field[]::new))
            .from(table(TABLE_NAME)).where(DSL.field(ACCESS_BOT_NAME).like("%" + botName + "%"));

    return jdbcTemplate.query(query.getSQL(), (rs, rowNum) -> {
      TelegramUser telegramUser = new TelegramUser();

      telegramUser.setAccess(rs.getBoolean(ACCESS));
      telegramUser.setUserId(rs.getLong(USER_ID));

      return telegramUser;
    }, query.getBindValues().toArray());
  }

}
