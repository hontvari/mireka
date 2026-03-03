package mireka.sieve.ast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.sieve.Interpreter.Token;
import mireka.sieve.Kind;
import mireka.sieve.Position;

public class Node {
    private final Logger logger = LoggerFactory.getLogger(Node.class);
    public Kind kind;
    public Position position;

    public Node(Token token) {
        this.kind = token.kind;
        this.position = token.position;
    }

    protected void trace() {
        logger.trace("Running {} at {}", kind, position);
    }
}
