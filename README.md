MyBatis SQL Mapper Framework for Java
=====================================

扩展新功能:
mapper.xml文件中定义select查询语句支持新字段tempPackage，可以把定义的select查询片段中的id作为ORM
映射类的类名自动生成到tempPackage.temp目录下。(注：tempPackage目录或子目录下一定要有一个.java文件，否则无效)

select查询片段中定义tempPackage则不可以再定义resultType或resultMap,示例：

<select id="selectItemBaseByProductsId" parameterType="BigInteger" tempPackage="com.tianyou.Mybatis">
        SELECT
        <include refid="ItemBaseMysqlColumn"/>
        FROM
        products,
        products_sourcing,
        products_platform,
        products_ezbuy,
        manufactures_mapping_ezbuy
        WHERE
        products.id=#{Id}
        and products.manufacturers_id=manufactures_mapping_ezbuy.manufacturers_id
        and products.id=products_sourcing.products_id
        and products.id=products_platform.products_id
        and products.id=products_ezbuy.products_id;
    </select>

