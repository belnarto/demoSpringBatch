package com.example.belnarto.config;

import com.example.belnarto.JobCompletionNotificationListener;
import com.example.belnarto.MyTaskTwo;
import com.example.belnarto.dto.Person;
import com.example.belnarto.itemprocessor.PersonItemProcessor;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Bean
    public FlatFileItemReader<Person> reader() {
        return new FlatFileItemReaderBuilder<Person>()
            .name("personItemReader")
            .resource(new ClassPathResource("sample-data.csv"))
            .delimited()
            .names(new String[]{"firstName", "lastName"})
            .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                setTargetType(Person.class);
            }})
            .build();
    }

    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Person>()
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
            .dataSource(dataSource)
            .build();
    }

    @Bean
    public Job importUserJob(JobBuilderFactory jobBuilderFactory,
        JobCompletionNotificationListener listener,
        @Qualifier("step1") Step step1,
        @Qualifier("step2") Step step2
    ) {
        return jobBuilderFactory.get("importUserJob")
            .incrementer(new RunIdIncrementer())
            .listener(listener)
            .flow(step1)
            .next(step2)
            .end()
            .build();
    }

    @Bean
    public Step step2(StepBuilderFactory stepBuilderFactory){
        return stepBuilderFactory.get("step2")
            .tasklet(new MyTaskTwo())
            .build();
    }

    @Bean
    public Step step1(StepBuilderFactory stepBuilderFactory,
        JdbcBatchItemWriter<Person> writer,
        FlatFileItemReader<Person> reader,
        PersonItemProcessor processor
    ) {
        return stepBuilderFactory.get("step1")
            .<Person, Person>chunk(5)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
    }
    
}
