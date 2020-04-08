module.exports = runtime => {
    const start = new Date().getMilliseconds();
    return {
        'Stopwatch.now': () => {
            return new Date().getMilliseconds() - start;
        }
    };
};