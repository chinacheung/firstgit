package cn.cheung.mp.mpfirst;

import cn.cheung.mp.mpfirst.dao.UserMapper;
import cn.cheung.mp.mpfirst.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MpCrudApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testSelectById() {
        User user = userMapper.selectById(1088248166370832385L);
        System.out.println(user);
    }

    @Test
    public void testSelectBatchIds() {
        List<Long> idList = Arrays.asList(1087982257332887553L, 1088248166370832385L, 1088250446457389058L);
        List<User> userList = userMapper.selectBatchIds(null);
        for (User user : userList) {
            System.out.println(user);
        }
    }

    @Test
    public void testSelectByMap() {
        Map<String, Object> columnMap = new HashMap<>();
        columnMap.put("name", "Bill%");
        columnMap.put("age", "27");
        List<User> userList = userMapper.selectByMap(columnMap);
        for (User user : userList) {
            System.out.println(user);
        }
    }

    // 名字包含“雨”，且年龄小于40岁
    // Preparing: SELECT id,create_time,name,manager_id,email,age FROM user WHERE (name LIKE ? AND age < ?)
    // Parameters: %雨%(String), 40(Integer)
    @Test
    public void testSelectList01() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", "雨");
        queryWrapper.lt("age", 40);
        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            System.out.println(user);
        }
    }

    // 名字包含“雨”，且年龄大于等于20小于等于40，且email不为NULL
    // Preparing: SELECT id,create_time,name,manager_id,email,age FROM user WHERE (name LIKE ? AND age >= ? AND age <= ? AND email IS NOT NULL)
    // Parameters: %雨%(String), 20(Integer), 40(Integer)
    @Test
    public void testSelectList02() {
        QueryWrapper<User> queryWrapper = Wrappers.query();
        queryWrapper.like("name", "雨").ge("age", 20).le("age", 40).isNotNull("email");
        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            System.out.println(user);
        }
    }

    // 名字包含“雨”，且年龄大于等于20小于等于40，且email不为NULL
    // Preparing: SELECT id,create_time,name,manager_id,email,age FROM user WHERE (name LIKE ? AND age BETWEEN ? AND ? AND email IS NOT NULL)
    // Parameters: %雨%(String), 20(Integer), 40(Integer)
    @Test
    public void testSelectList03() {
        QueryWrapper<User> queryWrapper = Wrappers.query();
        queryWrapper.like("name", "雨").between("age", 20, 40).isNotNull("email");
        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            System.out.println(user);
        }
    }

    // 名字为王姓，或年龄大于等于25，首先按照年龄降序排列，若年龄相同，则按照id升序排列
    // Preparing: SELECT id,create_time,name,manager_id,email,age FROM user WHERE (name LIKE ? OR age >= ?) ORDER BY age DESC , id ASC
    // Parameters: 王%(String), 25(Integer)
    @Test
    public void testSelectList04() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("name", "王").or().ge("age", 25).orderByDesc("age").orderByAsc("id");
        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            System.out.println(user);
        }
    }

    // 创建日期为2019年2月14日，且直属上级的名字为王姓
    // DATE(create_time) = '2019-02-14' AND manager_id IN (SELECT id FROM user WHERE name LIKE '王%')
    @Test
    public void testSelectList05() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        // Preparing: SELECT id,create_time,name,manager_id,email,age FROM user WHERE (DATE(create_time) = ? AND manager_id IN (SELECT id FROM user WHERE name LIKE '王%'))
        // Parameters: 2019-02-14(String)
//        queryWrapper.apply("DATE(create_time) = {0}", "2019-02-14").inSql("manager_id", "SELECT id FROM user WHERE name LIKE '王%'");

        // Preparing: SELECT id,create_time,name,manager_id,email,age FROM user WHERE (DATE(create_time) = '2019-02-14' AND manager_id IN (SELECT id FROM user WHERE name LIKE '王%'))
        // Parameters:
        queryWrapper.apply("DATE(create_time) = " + "'2019-02-14'").inSql("manager_id", "SELECT id FROM user WHERE name LIKE '王%'");

        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            System.out.println(user);
        }
    }

    // 创建日期为2019年2月14日，且直属上级的名字为王姓
    // DATE(create_time) = '2019-02-14' AND manager_id IN (SELECT id FROM user WHERE name LIKE '王%')
    // 演示 SQL 注入
    @Test
    public void testSelectList06() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        // Preparing: SELECT id,create_time,name,manager_id,email,age FROM user WHERE (DATE(create_time) = ? AND manager_id IN (SELECT id FROM user WHERE name LIKE '王%'))
        // Parameters: 2019-02-14 OR TRUE(String)
        // 仅查询出 1 条记录
//        queryWrapper.apply("DATE(create_time) = {0}", "2019-02-14 OR TRUE").inSql("manager_id", "SELECT id FROM user WHERE name LIKE '王%'");

        // Preparing: SELECT id,create_time,name,manager_id,email,age FROM user WHERE (DATE(create_time) = '2019-02-14' AND manager_id IN (SELECT id FROM user WHERE name LIKE '王%'))
        // Parameters:
        // 查询出 11 条记录
        queryWrapper.apply("DATE(create_time) = " + "'2019-02-14' OR TRUE").inSql("manager_id", "SELECT id FROM user WHERE name LIKE '王%'");

        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            System.out.println(user);
        }
    }

    // 名字为王姓且（年龄小于 40 或邮箱不为 NULL）
    // name LIKE '王%' AND (AGE < 40 OR email IS NOT NULL)
    // Preparing: SELECT id,create_time,name,manager_id,email,age FROM user WHERE name LIKE ? AND (age < ? OR email IS NOT NULL)
    // Parameters: 王%(String), 40(Integer)
    @Test
    public void testSelectList07() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.likeRight("name", "王").and(qw -> qw.lt("age", 40).or().isNotNull("email"));

        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            System.out.println(user);
        }
    }

    // 名字为王姓或（年龄大于 20 且小于 40 且邮箱不为 NULL）
    // name LIKE '王%' OR (age > 20 AND age < 40 AND email IS NOT NULL)
    // Preparing: SELECT id,create_time,name,manager_id,email,age FROM user WHERE (name LIKE ? OR ( (age > ? AND age < ? AND email IS NOT NULL) ))
    // Parameters: 王%(String), 20(Integer), 40(Integer)
    @Test
    public void testSelectList08() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.likeRight("name", "王").or(qw -> qw.gt("age", 20).lt("age", 40).isNotNull("email"));

        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            System.out.println(user);
        }
    }

    // （年龄小于 40 或邮箱不为 NULL）且名字为王姓
    // (age < 40 OR email IS NOT NULL) AND name LIKE '王%'
    // Preparing: SELECT id,create_time,name,manager_id,email,age FROM user WHERE (age < ? OR email IS NOT NULL) AND name LIKE ?
    // Parameters: 40(Integer), 王%(String)
    @Test
    public void testSelectList09() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.nested(qw -> qw.lt("age", 40).or().isNotNull("email")).likeRight("name", "王");

        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            System.out.println(user);
        }
    }

    // 年龄为 30、31、34 或 35
    // age IN (30, 31, 34, 35)
    // Preparing: SELECT id,create_time,name,manager_id,email,age FROM user WHERE (age IN (?,?,?,?))
    // Parameters: 30(Integer), 31(Integer), 34(Integer), 35(Integer)
    @Test
    public void testSelectList10() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

//        queryWrapper.inSql("age", "30, 31, 34, 35");
        queryWrapper.in("age", Arrays.asList(30, 31, 34, 35));
//        queryWrapper.in("age", 30, 31, 34, 35);

        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            System.out.println(user);
        }
    }

    // 年龄为 30、31、34 或 35，且显示第一条
    // age IN (30, 31, 34, 35) LIMIT 1
    // Preparing: SELECT id,create_time,name,manager_id,email,age FROM user WHERE (age IN (?,?,?,?)) LIMIT 1
    // Parameters: 30(Integer), 31(Integer), 34(Integer), 35(Integer)
    @Test
    public void testSelectList11() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("age", Arrays.asList(30, 31, 34, 35)).last("LIMIT 1");
        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            System.out.println(user);
        }
    }

    // 名字包含“雨”，且年龄小于40岁，只查询 id 和 name 列
    // Preparing: SELECT id,name FROM user WHERE name LIKE ? AND age < ?
    // Parameters: %雨%(String), 40(Integer)
    @Test
    public void testSelectList12() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.select("id", "name").like("name", "雨").lt("age", 40);

        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            System.out.println(user);
        }
    }

    // 名字包含“雨”，且年龄小于40岁，只查询 id 和 name 列
    // Preparing: SELECT id,name FROM user WHERE name LIKE ? AND age < ?
    // Parameters: %雨%(String), 40(Integer)
    @Test
    public void testSelectList13() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.select(User.class, info -> !info.getColumn().equals("id") && !info.getColumn().equals("name")).like("name", "雨").lt("age", 40);

        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            System.out.println(user);
        }
    }

    @Test
    public void testQueryWrapperEntity() {

    }

    // Preparing: SELECT id,create_time,name,manager_id,email,age FROM user WHERE (name = ? AND age = ?)
    // Parameters: 王天风(String), 25(Integer)
    @Test
    public void testQueryWrapperAllEq01() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        Map<String, Object> params = new HashMap<>();
        params.put("name", "");
        params.put("age", 25);
        queryWrapper.allEq(params);

        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            System.out.println(user);
        }
    }

    // Preparing: SELECT id,create_time,name,manager_id,email,age FROM user WHERE name = ? AND age IS NULL
    // Parameters: 王天风(String)
    @Test
    public void testQueryWrapperAllEq02() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        Map<String, Object> params = new HashMap<>();
        params.put("name", "王天风");
        params.put("age", null);
        queryWrapper.allEq(params);

        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            System.out.println(user);
        }
    }

    // Preparing: SELECT id,create_time,name,manager_id,email,age FROM user WHERE name = ?
    // Parameters: 王天风(String)
    @Test
    public void testQueryWrapperAllEq03() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        Map<String, Object> params = new HashMap<>();
        params.put("name", "王天风");
        params.put("age", null);
        queryWrapper.allEq(params, false);

        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            System.out.println(user);
        }
    }

    // Preparing: SELECT id,create_time,name,manager_id,email,age FROM user WHERE age = ?
    // Parameters: 25(Integer)
    @Test
    public void testQueryWrapperAllEq04() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        Map<String, Object> params = new HashMap<>();
        params.put("name", "王天风");
        params.put("age", 25);
        queryWrapper.allEq((key, value) -> !key.equals("name"), params);

        List<User> users = userMapper.selectList(queryWrapper);
        for (User user : users) {
            System.out.println(user);
        }
    }

    @Test
    public void testSelectMaps01() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.select("id", "name").like("name", "雨").lt("age", 40);

        List<Map<String, Object>> users = userMapper.selectMaps(queryWrapper);
        for (Map<String, Object> user : users) {
            System.out.println(user);
        }
    }

    @Test
    public void testSelectMaps02() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.select("AVG(age) AS avg_age", "MAX(age) AS max_age", "MIN(age) AS min_age")
                .groupBy("manager_id")
                .having("SUM(age) < {0}", 500);

        List<Map<String, Object>> resultList = userMapper.selectMaps(queryWrapper);
        for (Map<String, Object> result : resultList) {
            System.out.println(result);
        }
    }

    @Test
    public void testSelectObjs() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.select("id", "name").like("name", "雨").lt("age", 40);

        List<Object> idList = userMapper.selectObjs(queryWrapper);
        for (Object id : idList) {
            System.out.println(id);
        }
    }

    @Test
    public void testSelectCount() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.like("name", "雨").lt("age", 40);

        Integer count = userMapper.selectCount(queryWrapper);
        System.out.println("名字包含雨且年龄小于 40 的用户数：" + count);
    }

    @Test
    public void testSelectOne() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.select("id", "name", "age").eq("name", "刘红雨").eq("age", 32);

        User user = userMapper.selectOne(queryWrapper);
        System.out.println(user);
    }

    @Test
    public void testGetLambdaQueryWrapper() {
//        LambdaQueryWrapper<User> lambdaQueryWrapper = new QueryWrapper<User>().lambda();
//        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<User> lambdaQueryWrapper = Wrappers.lambdaQuery();
    }

    @Test
    public void testGit() {
        System.out.println("使用 Git。");
    }
}
