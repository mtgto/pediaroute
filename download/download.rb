# bundle exec ruby download.rb 20140208

require 'nokogiri'
require 'open-uri'

class Download
  def initialize(last_version)
    @last_version = last_version
  end

  def exec(cmd)
    STDERR.puts "executing \"#{cmd}\""
    exit(1) unless system(cmd)
  end

  def start
    doc = Nokogiri::HTML(open('http://dumps.wikimedia.org/jawiki/'))
    latest_version = doc.css('a')[-2].text
    exit(1) if latest_version == @last_version
    exec('wget --quiet http://dumps.wikimedia.org/jawiki/latest/jawiki-latest-page.sql.gz')
    exec('wget --quiet http://dumps.wikimedia.org/jawiki/latest/jawiki-latest-pagelinks.sql.gz')
    exec('echo "DROP TABLE IF EXISTS page; DROP TABLE IF EXISTS pagelinks;" | mysql -u user wikipedia')
    exec('gzcat jawiki-latest-page.sql.gz | time mysql -u user wikipedia')
    exec('gzcat jawiki-latest-pagelinks.sql.gz | time mysql -u user wikipedia')
    exec('rm jawiki-latest-page.sql.gz')
    exec('rm jawiki-latest-pagelinks.sql.gz')
    exec("echo '#{latest_version}' > LAST_VERSION")
  end
end

d = Download.new(ARGV[0])
d.start