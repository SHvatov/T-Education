package ru.tbank.edu.ab.sem3;

public class DesignForExtension {

    public static abstract class Transactional<R> {

        protected abstract R executeInTransaction(Transaction transaction);

        protected void onCommit(Transaction transaction) {

        }

        protected void onRollback(Throwable exception) {

        }

        protected final R executeInTransaction() {
            var transaction = Transaction.create();
            try {
                var result = executeInTransaction(transaction);
                onCommit(transaction);
                transaction.commit();
                return result;
            } catch (Throwable exception) {
                onRollback(exception);
                transaction.rollback();
                throw exception;
            } finally {
                transaction.close();
            }
        }
    }

    public static final class SavePersonAction extends Transactional<Person> {
        @Override
        protected Person executeInTransaction(Transaction transaction) {
            return new Person("Sergey", "Khvatov", 25);
        }

        @Override
        protected void onRollback(Throwable exception) {
            System.out.printf("Ошибка при сохранении пользователя: %s", exception.getMessage());
            exception.printStackTrace();
        }
    }

    public static final class Transaction implements AutoCloseable {

        public void commit() {

        }

        public void rollback() {

        }

        @Override
        public void close() {

        }

        public static Transaction create() {
            return new Transaction();
        }
    }
}
